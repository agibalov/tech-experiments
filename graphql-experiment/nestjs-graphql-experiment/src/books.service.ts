import { Injectable } from '@nestjs/common';
import { Book } from './graphql';

@Injectable()
export class BooksService {
    private readonly books: Book[] = [];

    constructor() {
        for (let i = 0; i < 3; ++i) {
            this.books.push({
                title: `Book #${i + 1}`,
                author: `Author ${i + 1}`
            });
        }
    }

    async getAllBooks(): Promise<Book[]> {
        return this.books;
    }

    async getBook(bookId: number): Promise<Book> {
        return this.books[bookId];
    }
}
