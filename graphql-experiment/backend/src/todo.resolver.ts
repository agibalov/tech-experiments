import { Resolver, Query, Args, Mutation, Subscription } from '@nestjs/graphql';
import { Todo } from './graphql';
import { PubSub } from 'graphql-subscriptions';
import { Inject } from '@nestjs/common';

@Resolver('Todos')
export class TodoResolver {
    private todos: Todo[] = [
        { id: '1', text: 'Get some coffee' },
        { id: '2', text: 'Get some milk '}
    ];

    constructor(@Inject('PUB_SUB') private readonly pubSub: PubSub) {
    }

    @Query('todos')
    async getAllTodos(): Promise<Todo[]> {
        return this.todos;
    }

    @Query('todo')
    async getBook(
        @Args('id') id: string): Promise<Todo> {

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

        let todo = this.todos.find(t => t.id === id);
        if (todo === undefined) {
            todo = { id, text };
            this.todos.push(todo);
            await this.pubSub.publish('todoAdded', {
                todoAdded: todo
            });
        } else {
            todo.text = text;
        }
        return todo;
    }

    @Mutation('deleteTodo')
    async deleteTodo(
        @Args('id') id: string): Promise<Todo> {

        this.todos = this.todos.filter(t => t.id !== id);
        return null;
    }

    @Subscription('todoAdded')
    todoAdded() {
        return this.pubSub.asyncIterator<Todo>('todoAdded');
    }
}
