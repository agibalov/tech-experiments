import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { GraphQLModule } from '@nestjs/graphql';
import { join } from 'path';
import { BooksResolver } from './books.resolver';
import { BooksService } from './books.service';

@Module({
    imports: [
        GraphQLModule.forRoot({
            typePaths: ['./**/*.graphql'],
            definitions: {
                path: join(process.cwd(), 'src/graphql.ts')
            }
        })
    ],
    controllers: [AppController],
    providers: [
        AppService,
        BooksService,
        BooksResolver
    ],
})
export class AppModule {
}
