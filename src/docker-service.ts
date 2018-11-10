import * as Docker from 'dockerode';
import { ContainerCreateOptions } from 'dockerode';
import { Container } from 'dockerode';

export class DockerService {
    constructor(private readonly docker: Docker) {
    }

    async start(options: ContainerCreateOptions & { Image: string }) {
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
        console.log(`Starter ${container.id}`);

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
