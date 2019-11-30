import { Component, OnDestroy, OnInit } from '@angular/core';
import { Apollo } from 'apollo-angular';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { AllTodosGQL, AllTodosQuery, CreateTodoGQL, DeleteTodoGQL, Todo, TodoChangedGQL,
  TodoEventKind } from './graphql';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, OnDestroy {
  private allTodosQueryObservableSubscription: Subscription;
  todos: Todo[];

  readonly newTodoForm = new FormGroup({
    text: new FormControl('', Validators.required)
  });

  constructor(
    private readonly apollo: Apollo,
    private readonly allTodosGql: AllTodosGQL,
    private readonly createTodoGql: CreateTodoGQL,
    private readonly deleteTodoGql: DeleteTodoGQL,
    private readonly todoChangedGql: TodoChangedGQL) {
  }

  ngOnInit(): void {
    const allTodosQueryRef = this.apollo.watchQuery<AllTodosQuery>({
      query: this.allTodosGql.document
    });

    this.allTodosQueryObservableSubscription = allTodosQueryRef.valueChanges.subscribe(result => {
      this.todos = result.data.todos;
    });

    allTodosQueryRef.subscribeToMore({
      document: this.todoChangedGql.document,
      updateQuery: (previousQueryResult, options) => {
        const todoChanged = options.subscriptionData.data.todoChanged;
        if (todoChanged.kind === TodoEventKind.Put) {
          return {
            ...previousQueryResult,
            todos: [...previousQueryResult.todos.filter(t => t.id !== todoChanged.todo.id), todoChanged.todo]
          };
        }

        if (todoChanged.kind === TodoEventKind.Delete) {
          return {
            ...previousQueryResult,
            todos: previousQueryResult.todos.filter(t => t.id !== todoChanged.todo.id)
          };
        }

        throw new Error(`Unknown kind ${todoChanged.kind}`);
      }});
  }

  ngOnDestroy(): void {
    this.allTodosQueryObservableSubscription.unsubscribe();
  }

  createTodo() {
    const formValue: { text: string } = this.newTodoForm.getRawValue();
    this.newTodoForm.reset();

    const todoId = new Date().toISOString();
    this.createTodoGql.mutate({
      id: todoId,
      text: formValue.text
    }, {
      refetchQueries: [{
        query: this.allTodosGql.document
      }],
      optimisticResponse: {
        __typename: 'Mutation',
        putTodo: {
          __typename: 'Todo',
          id: todoId,
          text: `OPTIMISTIC - ${formValue.text}`
        }
      },
      update: (proxy, mutationResult) => {
        const data = proxy.readQuery<AllTodosQuery>({
          query: this.allTodosGql.document
        });
        if (data.todos.some(t => t.id === mutationResult.data.putTodo.id)) {
          console.log('The new todo is already there - probably because of todoAdded update');
          return;
        }

        data.todos.push(mutationResult.data.putTodo);
        proxy.writeQuery({
          query: this.allTodosGql.document, data
        });
      }
    }).subscribe(result => {
      console.log('createTodo result', result);
    });
  }

  deleteTodo(id: string) {
    this.deleteTodoGql.mutate({
      id
    }, {
      refetchQueries: [{
        query: this.allTodosGql.document
      }],
      optimisticResponse: {
        __typename: 'Mutation',
        deleteTodo: null
      },
      update: (proxy, mutationResult) => {
        const data = proxy.readQuery<AllTodosQuery>({
          query: this.allTodosGql.document
        });
        data.todos = data.todos.filter(t => t.id !== id);
        proxy.writeQuery({
          query: this.allTodosGql.document, data
        });
      }
    }).subscribe(result => {
      console.log('deleteTodo result', result);
    });
  }
}
