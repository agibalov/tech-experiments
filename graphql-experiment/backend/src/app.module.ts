import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { GraphQLModule } from '@nestjs/graphql';
import { join } from 'path';
import { TodoResolver } from './todo.resolver';
import { PubSub } from 'graphql-subscriptions';

@Module({
    imports: [
        GraphQLModule.forRoot({
            typePaths: ['./**/*.graphql'],
            definitions: {
                path: join(process.cwd(), 'src/graphql.ts')
            },
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
