FROM node:10.16.2-alpine
EXPOSE 3000
COPY . /app
WORKDIR /app
RUN yarn install --dev
RUN yarn graphql-codegen
RUN yarn lint
RUN yarn test
RUN yarn fe:build
RUN yarn be:build
CMD ["yarn", "start"]
