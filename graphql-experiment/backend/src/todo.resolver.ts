import { Resolver, Query, Args, Mutation, Subscription } from '@nestjs/graphql';
import { Todo, TodoEvent, TodoEventKind } from './graphql';
import { PubSub } from 'graphql-subscriptions';
import { Inject } from '@nestjs/common';

@Resolver('Todos')
export class TodoResolver {
  private todos: Todo[] = [
    {id: '1', text: 'Get some coffee'},
    {id: '2', text: 'Get some milk'}
  ];

  constructor(@Inject('PUB_SUB') private readonly pubSub: PubSub) {
  }

  private static async sleep() {
    await new Promise(resolve => setTimeout(resolve, 1000));
  }

  @Query('todos')
  async getAllTodos(): Promise<Todo[]> {
    await TodoResolver.sleep();
    return this.todos;
  }

  @Query('todo')
  async getTodo(
    @Args('id') id: string): Promise<Todo> {

    await TodoResolver.sleep();

    const todo = this.todos.find(t => t.id === id);
    if (todo === undefined) {
      return null;
    }
    return todo;
  }

  @Mutation('putTodo')
  async putTodo(
    @Args('id') id: string,
    @Args('text') text: string): Promise<Todo> {

    await TodoResolver.sleep();

    let todo = this.todos.find(t => t.id === id);
    if (todo === undefined) {
      todo = {id, text};
      this.todos.push(todo);
      await this.pubSub.publish('todoChanged', {
        todoChanged: {
          kind: TodoEventKind.Put,
          todo
        }
      });
    } else {
      todo.text = text;
    }
    return todo;
  }

  @Mutation('deleteTodo')
  async deleteTodo(
    @Args('id') id: string): Promise<Todo> {

    await TodoResolver.sleep();

    const todo = this.todos.find(t => t.id === id);
    if (todo === undefined) {
      return null;
    }

    this.todos = this.todos.filter(t => t.id !== id);

    await this.pubSub.publish('todoChanged', {
      todoChanged: {
        kind: TodoEventKind.Delete,
        todo
      }
    });

    return todo;
  }

  @Subscription('todoChanged')
  todoChanged() {
    return this.pubSub.asyncIterator<TodoEvent>('todoChanged');
  }
}
