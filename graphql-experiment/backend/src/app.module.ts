import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { GraphQLModule } from '@nestjs/graphql';
import { TodoResolver } from './todo.resolver';
import { PubSub } from 'graphql-subscriptions';
import { ServeStaticModule } from '@nestjs/serve-static';
import { resolve } from 'path';

@Module({
  imports: [
    ServeStaticModule.forRoot({
      rootPath: resolve('frontend/dist/frontend/')
    }),
    GraphQLModule.forRoot({
      typePaths: ['shared/*.graphql'],
      installSubscriptionHandlers: true
    })
  ],
  controllers: [AppController],
  providers: [
    AppService,
    TodoResolver,
    {
      provide: 'PUB_SUB',
      useValue: new PubSub()
    }
  ],
})
export class AppModule {
}
