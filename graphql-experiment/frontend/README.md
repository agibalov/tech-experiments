# frontend

Before starting it, start BE and run `yarn graphql-codegen` here to generate GraphQL types. Note that [typescript-apollo-angular](https://graphql-code-generator.com/docs/plugins/typescript-apollo-angular) codegen plugin will only generate query/mutation/subscription classes for queries, mutations and subscriptions described in `src/app/todo-operations.graphql`. You should manually add generated classes as providers to `AppModule`.

* `yarn start` to run in dev mode.
