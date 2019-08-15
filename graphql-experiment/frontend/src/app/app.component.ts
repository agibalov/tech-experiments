import { Component, OnDestroy, OnInit } from '@angular/core';
import { Apollo } from 'apollo-angular';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { AllTodosGQL, CreateTodoGQL, DeleteTodoGQL, Todo, TodoAddedGQL } from './graphql';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, OnDestroy {
  private allTodosQueryObservableSubscription: Subscription;
  private todoAddedSubscriptionObservableSubscription: Subscription;
  todos: Todo[];
  mostRecentTodo: Todo = null;

  readonly newTodoForm = new FormGroup({
    text: new FormControl('', Validators.required)
  });

  constructor(
    private readonly apollo: Apollo,
    private readonly allTodosGql: AllTodosGQL,
    private readonly createTodoGql: CreateTodoGQL,
    private readonly deleteTodoGql: DeleteTodoGQL,
    private readonly todoAddedGql: TodoAddedGQL) {
  }

  ngOnInit(): void {
    this.allTodosQueryObservableSubscription = this.allTodosGql.watch().valueChanges
      .subscribe(result => {
        this.todos = result.data.todos;
      });

    this.todoAddedSubscriptionObservableSubscription = this.todoAddedGql.subscribe()
      .subscribe(result => {
        this.mostRecentTodo = result.data.todoAdded;
      });
  }

  ngOnDestroy(): void {
    this.allTodosQueryObservableSubscription.unsubscribe();
    this.todoAddedSubscriptionObservableSubscription.unsubscribe();
  }

  createTodo() {
    const formValue: { text: string } = this.newTodoForm.getRawValue();
    this.newTodoForm.reset();

    this.createTodoGql.mutate({
      id: `${new Date().toISOString()}`,
      text: formValue.text
    }, {
      refetchQueries: [{
        query: this.allTodosGql.document
      }]
    }).subscribe(result => {
      console.log('result', result);
    });
  }

  deleteTodo(id: string) {
    this.deleteTodoGql.mutate({
      id
    }, {
      refetchQueries: [{
        query: this.allTodosGql.document
      }]
    }).subscribe(result => {
      console.log('result', result);
    });
  }
}
