import { Component, OnInit } from '@angular/core';
import { Apollo } from 'apollo-angular';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AllTodosQuery, DeleteTodoMutation, PutTodoMutation, Todo } from './todos';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  sum: number;
  todos: Todo[];

  readonly newTodoForm = new FormGroup({
    text: new FormControl('', Validators.required)
  });

  constructor(
    private readonly apollo: Apollo,
    private readonly allTodosQuery: AllTodosQuery,
    private readonly putTodoMutation: PutTodoMutation,
    private readonly deleteTodoMutation: DeleteTodoMutation) {
  }

  ngOnInit(): void {
    this.allTodosQuery.watch().valueChanges.subscribe(result => {
      this.todos = result.data.todos;
    });
  }

  createTodo() {
    const formValue: { text: string } = this.newTodoForm.getRawValue();
    this.newTodoForm.reset();

    this.putTodoMutation.mutate({
      id: `${new Date().toISOString()}`,
      text: formValue.text
    }, {
      refetchQueries: [{
        query: this.allTodosQuery.document
      }]
    }).subscribe(result => {
      console.log('result', result);
    });
  }

  deleteTodo(id: string) {
    this.deleteTodoMutation.mutate({
      id
    }, {
      refetchQueries: [{
        query: this.allTodosQuery.document
      }]
    }).subscribe(result => {
      console.log('result', result);
    });
  }
}
