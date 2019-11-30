import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { AppComponent } from './app.component';
import { HttpClientModule } from '@angular/common/http';
import { HttpLink, HttpLinkModule } from 'apollo-angular-link-http';
import { Apollo, ApolloModule } from 'apollo-angular';
import { InMemoryCache } from 'apollo-cache-inmemory';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { concat, NextLink, Operation, split } from 'apollo-link';
import { WebSocketLink } from 'apollo-link-ws';
import { getMainDefinition } from 'apollo-utilities';
import { AllTodosGQL, CreateTodoGQL, DeleteTodoGQL, TodoChangedGQL } from './graphql';
import { OperationDefinitionNode } from 'graphql';

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    ApolloModule,
    HttpLinkModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [
    AllTodosGQL,
    CreateTodoGQL,
    DeleteTodoGQL,
    TodoChangedGQL
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
  constructor(apollo: Apollo, httpLink: HttpLink) {
    const httpLinkHandler = httpLink.create({
      uri: 'http://localhost:3000/graphql'
    });
    const webSocketLink = new WebSocketLink({
      uri: 'ws://localhost:3000/graphql',
      options: {
        reconnect: true,
        reconnectionAttempts: 100
      }
    });

    const loggingHttpLink = concat((operation: Operation, forward?: NextLink) => {
      console.log('LOG', operation);
      return forward(operation);
    }, httpLinkHandler);

    const link = split(
      ({ query }) => {
        const { kind, operation } = getMainDefinition(query) as OperationDefinitionNode;
        return kind === 'OperationDefinition' && operation === 'subscription';
      },
      webSocketLink,
      loggingHttpLink);

    apollo.create({
      link,
      cache: new InMemoryCache()
    });
  }
}
