import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { AppComponent } from './app.component';
import { HttpClientModule } from '@angular/common/http';
import { HttpLink, HttpLinkModule } from 'apollo-angular-link-http';
import { Apollo, ApolloModule } from 'apollo-angular';
import { InMemoryCache } from 'apollo-cache-inmemory';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AllTodosQuery, DeleteTodoMutation, PutTodoMutation, TodoAddedSubscription } from './todos';
import { split } from 'apollo-link';
import { WebSocketLink } from 'apollo-link-ws';
import { getMainDefinition } from 'apollo-utilities';

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
    AllTodosQuery,
    PutTodoMutation,
    DeleteTodoMutation,
    TodoAddedSubscription
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

    const link = split(
      ({ query }) => {
        const { kind, operation } = getMainDefinition(query);
        return kind === 'OperationDefinition' && operation === 'subscription';
      },
      webSocketLink,
      httpLinkHandler);

    apollo.create({
      link,
      cache: new InMemoryCache()
    });
  }
}
