import { Resolver, Query, Args } from '@nestjs/graphql';
import { Book } from './graphql';
import { BooksService } from './books.service';

@Resolver('Books')
export class BooksResolver {
    constructor(private readonly booksService: BooksService) {
    }

    @Query('books')
    async resolveBooks(): Promise<Book[]> {
        await new Promise(resolve => {
            setTimeout(resolve, 300);
        });
        return await this.booksService.getAllBooks();
    }

    @Query('book')
    async resolveBook(@Args('bookId') bookId: number): Promise<Book> {
        return await this.booksService.getBook(bookId);
    }
}
