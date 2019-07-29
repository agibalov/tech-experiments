import { Component, OnInit } from '@angular/core';
import { Apollo } from 'apollo-angular';
import gql from 'graphql-tag';

interface Book {
  title: string;
  author: string;
}

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  books: Book[];

  constructor(private readonly apollo: Apollo) {
  }

  ngOnInit(): void {
    this.apollo.query<{ books: Book[] }>({
      query: gql`
          {
              books { title author }
          }
      `
    }).subscribe(result => {
      console.log('result', result);
      this.books = result.data.books;
    });
  }
}
