import * as Docker from 'dockerode';
import { ContainerCreateOptions } from 'dockerode';
import { Container } from 'dockerode';
import * as makeNano from 'nano';

export interface ContainerDescription {
    readonly containerCreateOptions: ContainerCreateOptions;
    checkAvailability(): Promise<boolean>;
}

export class CouchDBContainerDescription implements ContainerDescription {
    constructor(
        private readonly username: string,
        private readonly password: string) {
    }

    get containerCreateOptions(): ContainerCreateOptions {
        return {
            Image: 'couchdb:2.2.0',
            Tty: true,
            Env: [
                `COUCHDB_USER=${this.username}`,
                `COUCHDB_PASSWORD=${this.password}`
            ],
            ExposedPorts: {
                '5984/tcp': {}
            },
            HostConfig: {
                PortBindings: {
                    '5984/tcp': [
                        { HostPort: '5984' }
                    ]
                }
            }
        };
    }

    async checkAvailability(): Promise<boolean> {
        const nano = makeNano(`http://${this.username}:${this.password}@localhost:5984`);
        try {
            await nano.db.list();
        } catch(e) {
            return false;
        }
        return true;
    }
};

export class DockerService {
    private static readonly MAX_AVAILABILITY_ATTEMPTS = 100;
    private static readonly AVAILABILITY_CHECK_PERIOD = 1000;

    constructor(private readonly docker: Docker) {
    }

    async start(containerDescription: ContainerDescription) {
        const options = containerDescription.containerCreateOptions;
        const image = await this.docker.getImage(options.Image);
        try {
            await image.inspect();
        } catch(e) {
            if(e.statusCode === 404) {
                console.log(`There's no local image ${options.Image}, going to pull`);
                const stream = await this.docker.pull(options.Image, {});
                stream.on('data', chunk => {
                    console.log('PROGRESS', chunk.toString());
                });
                await new Promise(resolve => stream.on('end', resolve));
                console.log(`Finished pulling ${options.Image}`);
            } else {
                throw e;
            }
        }

        console.log(`Creating a container ${options.Image}`);
        const container = await this.docker.createContainer(Object.assign({
            Tty: true
        }, options));
        console.log(`Created a container: ${container.id}`);

        const stream = await container.attach({
            stream: true,
            stdout: true,
            stderr: true
        });
        stream.pipe(process.stdout);

        console.log(`Starting ${container.id}`);
        await container.start();
        console.log(`Started ${container.id}`);

        console.log(`Waiting for service to become available`);
        let isAvailable = false;
        for(let i = 0; i < DockerService.MAX_AVAILABILITY_ATTEMPTS; ++i) {
            isAvailable = await containerDescription.checkAvailability();
            if(isAvailable) {
                break;
            }

            await new Promise(resolve => setTimeout(resolve, DockerService.AVAILABILITY_CHECK_PERIOD));
        }

        if(!isAvailable) {
            await this.stop(container);
            throw new Error('Service is not available');
        }

        console.log('Service is available!');

        return container;
    }

    async stop(container: Container) {
        console.log(`Stopping ${container.id}...`);
        await container.stop();
        console.log(`Stopped ${container.id}`);

        console.log(`Removing ${container.id}`);
        await container.remove();
        console.log(`Removed ${container.id}`);
    }
}
