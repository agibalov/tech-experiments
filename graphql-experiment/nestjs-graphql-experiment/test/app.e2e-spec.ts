import { Test, TestingModule } from '@nestjs/testing';
import * as request from 'supertest';
import { AppModule } from './../src/app.module';
import { INestApplication } from '@nestjs/common';
import { ApolloServerTestClient, createTestClient } from 'apollo-server-testing';
import { GraphQLModule } from '@nestjs/graphql';
import gql from 'graphql-tag';

describe('AppController (e2e)', () => {
    let app: INestApplication;
    let apolloClient: ApolloServerTestClient;

    beforeEach(async () => {
        const moduleFixture: TestingModule = await Test.createTestingModule({
            imports: [AppModule],
        }).compile();

        app = moduleFixture.createNestApplication();
        await app.init();

        const module = moduleFixture.get<GraphQLModule>(GraphQLModule);
        apolloClient = createTestClient((module as any).apolloServer);
    });

    it('/ (GET)', () => {
        return request(app.getHttpServer())
            .get('/')
            .expect(200)
            .expect('Hello World!');
    });

    describe('GraphQL API', () => {
        it('should work via raw HTTP', async () => {
            const response = await request(app.getHttpServer())
                .post('/graphql')
                .send({
                    operationName: null,
                    query: `{
                      books {
                        title
                        author
                      }
                    }`,
                    variables: {}
                });

            expect(response.status).toBe(200);
            expect(response.body).toEqual({
                data: {
                    books: [
                        { author: 'Author 1', title: 'Book #1' },
                        { author: 'Author 2', title: 'Book #2' },
                        { author: 'Author 3', title: 'Book #3' }
                    ]
                }
            });
        });

        it('should work via Apollo client', async () => {
            const response = await apolloClient.query({
                query: gql`{
                    books {
                        title
                        author
                    }
                }`,
                variables: {}
            });
            expect(response.data).toEqual({
                books: [
                    { author: 'Author 1', title: 'Book #1' },
                    { author: 'Author 2', title: 'Book #2' },
                    { author: 'Author 3', title: 'Book #3' }
                ]
            });
        });
    });
});
