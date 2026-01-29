#!/usr/bin/env node

// Lightdash Bootstrap Script
// Automates: user registration, org setup, project creation with ClickHouse warehouse
//
// This script fully automates Lightdash setup including:
// 1. User registration
// 2. Organization creation
// 3. Project creation with ClickHouse warehouse connection

const http = require('http');

const LIGHTDASH_URL = process.env.LIGHTDASH_URL || 'http://localhost:8080';
const SITE_URL = process.env.SITE_URL || 'http://localhost:3000';
const ADMIN_EMAIL = process.env.ADMIN_EMAIL || 'admin@example.com';
const ADMIN_PASSWORD = process.env.ADMIN_PASSWORD || 'admin123';
const ADMIN_FIRST_NAME = process.env.ADMIN_FIRST_NAME || 'Admin';
const ADMIN_LAST_NAME = process.env.ADMIN_LAST_NAME || 'User';
const ORG_NAME = process.env.ORG_NAME || 'Analytics';
const JOB_TITLE = process.env.JOB_TITLE || 'Analytics Engineer';

// ClickHouse warehouse configuration
const CLICKHOUSE_HOST = process.env.CLICKHOUSE_HOST || 'clickhouse';
const CLICKHOUSE_PORT = parseInt(process.env.CLICKHOUSE_PORT || '8123', 10);
const CLICKHOUSE_USER = process.env.CLICKHOUSE_USER || 'default';
const CLICKHOUSE_PASSWORD = process.env.CLICKHOUSE_PASSWORD || 'clickhouse';
const CLICKHOUSE_SCHEMA = process.env.CLICKHOUSE_SCHEMA || 'analytics';
const PROJECT_NAME = process.env.PROJECT_NAME || 'Analytics';

let sessionCookie = '';

function makeRequest(method, path, body = null) {
    return new Promise((resolve, reject) => {
        const url = new URL(path, LIGHTDASH_URL);
        const options = {
            hostname: url.hostname,
            port: url.port || 80,
            path: url.pathname,
            method: method,
            headers: {
                'Content-Type': 'application/json',
            }
        };

        if (sessionCookie) {
            options.headers['Cookie'] = sessionCookie;
        }

        const req = http.request(options, (res) => {
            let data = '';

            // Capture session cookie
            const setCookie = res.headers['set-cookie'];
            if (setCookie) {
                sessionCookie = setCookie.map(c => c.split(';')[0]).join('; ');
            }

            res.on('data', chunk => data += chunk);
            res.on('end', () => {
                try {
                    const json = data ? JSON.parse(data) : {};
                    resolve({ status: res.statusCode, data: json, headers: res.headers });
                } catch (e) {
                    resolve({ status: res.statusCode, data: data, headers: res.headers });
                }
            });
        });

        req.on('error', reject);

        if (body) {
            req.write(JSON.stringify(body));
        }
        req.end();
    });
}

async function waitForLightdash() {
    console.log('Waiting for Lightdash to be ready...');
    for (let i = 0; i < 60; i++) {
        try {
            const res = await makeRequest('GET', '/api/v1/health');
            if (res.status === 200) {
                console.log('Lightdash is ready!');
                return true;
            }
        } catch (e) {
            // ignore
        }
        console.log('  ...waiting');
        await new Promise(r => setTimeout(r, 5000));
    }
    throw new Error('Lightdash did not become ready in time');
}

async function checkIfUserExists() {
    try {
        const res = await makeRequest('GET', '/api/v1/user');
        // If we can get user info, user exists and we're logged in
        if (res.status === 200 && res.data?.results?.userUuid) {
            return res.data.results;
        }
    } catch (e) {
        // ignore
    }
    return null;
}

async function login() {
    const res = await makeRequest('POST', '/api/v1/login', {
        email: ADMIN_EMAIL,
        password: ADMIN_PASSWORD
    });
    if (res.status === 200) {
        console.log('Logged in successfully!');
        return res.data?.results;
    }
    return null;
}

async function checkIfProjectExists() {
    try {
        const res = await makeRequest('GET', '/api/v1/org/projects');
        if (res.status === 200 && res.data?.results?.length > 0) {
            return res.data.results[0];
        }
    } catch (e) {
        // ignore
    }
    return null;
}

async function waitForJob(jobUuid, maxAttempts = 60) {
    console.log(`  Waiting for job ${jobUuid}...`);
    for (let i = 0; i < maxAttempts; i++) {
        const res = await makeRequest('GET', `/api/v1/jobs/${jobUuid}`);
        if (res.status === 200) {
            const job = res.data?.results;
            if (job?.jobStatus === 'DONE') {
                console.log('  Job completed successfully!');
                return job;
            } else if (job?.jobStatus === 'ERROR') {
                throw new Error(`Job failed: ${JSON.stringify(job.steps)}`);
            }
            console.log(`  Job status: ${job?.jobStatus}...`);
        }
        await new Promise(r => setTimeout(r, 2000));
    }
    throw new Error('Job did not complete in time');
}

async function createPersonalAccessToken() {
    console.log('Creating personal access token for CLI...');
    const res = await makeRequest('POST', '/api/v1/user/me/personal-access-tokens', {
        description: 'Bootstrap CLI token',
        expiresAt: null,
        autoGenerated: false
    });
    if (res.status === 200 && res.data?.results?.token) {
        console.log('  Personal access token created');
        return res.data.results.token;
    }
    throw new Error('Failed to create personal access token: ' + JSON.stringify(res.data));
}

async function writeBootstrapOutput(projectUuid, token) {
    // Write output file with project UUID and token for CLI upload
    const fs = require('fs');
    const output = {
        projectUuid: projectUuid,
        token: token,
        lightdashUrl: SITE_URL
    };
    const outputPath = '/tmp/lightdash-bootstrap-output.json';
    fs.writeFileSync(outputPath, JSON.stringify(output, null, 2));
    console.log(`  Bootstrap output written to ${outputPath}`);
}

async function createProject() {
    const projectPayload = {
        name: PROJECT_NAME,
        type: 'DEFAULT',
        dbtConnection: {
            type: 'dbt',
            target: 'dev',
            environment: [],
            selector: ''
        },
        dbtVersion: 'v1.4',
        warehouseConnection: {
            type: 'clickhouse',
            schema: CLICKHOUSE_SCHEMA,
            host: CLICKHOUSE_HOST,
            user: CLICKHOUSE_USER,
            password: CLICKHOUSE_PASSWORD,
            requireUserCredentials: false,
            port: CLICKHOUSE_PORT,
            secure: false,
            timeoutSeconds: 300
        }
    };

    console.log('  Project payload:', JSON.stringify(projectPayload, null, 2));
    const res = await makeRequest('POST', '/api/v1/org/projects/precompiled', projectPayload);

    console.log('  Project creation response status:', res.status);
    if (res.status === 200 && res.data?.results?.jobUuid) {
        const jobUuid = res.data.results.jobUuid;
        console.log(`  Project creation job started: ${jobUuid}`);
        return await waitForJob(jobUuid);
    } else {
        console.log('Project creation response:', res.status);
        console.log('Response data:', JSON.stringify(res.data, null, 2));
        if (res.data?.error?.message) {
            console.log('Error message:', res.data.error.message);
        }
        throw new Error('Failed to create project: ' + (res.data?.error?.message || 'unknown error'));
    }
}

async function main() {
    console.log('=== Lightdash Bootstrap ===');
    console.log(`URL: ${LIGHTDASH_URL}`);
    console.log(`Admin: ${ADMIN_EMAIL}`);

    await waitForLightdash();

    // Check if already set up by trying to get user with session
    let existingUser = await checkIfUserExists();

    // If not logged in, try to login (user may exist from previous run)
    if (!existingUser) {
        console.log('Attempting to login with existing credentials...');
        const loginResult = await login();
        if (loginResult) {
            existingUser = await checkIfUserExists();
        }
    }

    if (existingUser && existingUser.isSetupComplete) {
        console.log('Lightdash user already set up.');
        console.log(`User: ${existingUser.email}`);
        console.log(`Organization: ${existingUser.organizationName}`);

        // Still check if project needs to be created
        console.log('Checking for existing project...');
        let existingProject = await checkIfProjectExists();
        if (!existingProject) {
            console.log('Creating project with ClickHouse warehouse...');
            try {
                const job = await createProject();
                console.log(`Project created! UUID: ${job.projectUuid}`);
                existingProject = await checkIfProjectExists();
            } catch (err) {
                console.log('Project creation failed:', err.message);
                console.log('You may need to create the project manually.');
            }
        } else {
            console.log(`Project already exists: ${existingProject.name} (${existingProject.projectUuid})`);
        }

        // Create token and write output for CLI upload
        if (existingProject) {
            try {
                const token = await createPersonalAccessToken();
                await writeBootstrapOutput(existingProject.projectUuid, token);
            } catch (err) {
                console.log('Token creation failed:', err.message);
            }
        }

        console.log('');
        console.log('=== Bootstrap Complete ===');
        console.log(`Lightdash is ready at: ${SITE_URL}`);
        return;
    }

    console.log('Setting up Lightdash (fresh install)...');

    // Step 1: Register admin user
    // API: POST /api/v1/user
    console.log('Step 1: Registering admin user...');
    const registerRes = await makeRequest('POST', '/api/v1/user', {
        firstName: ADMIN_FIRST_NAME,
        lastName: ADMIN_LAST_NAME,
        email: ADMIN_EMAIL,
        password: ADMIN_PASSWORD
    });

    if (registerRes.status === 200) {
        console.log('User registered successfully!');
        console.log(`User UUID: ${registerRes.data?.results?.userUuid}`);
    } else {
        console.log('Registration response:', registerRes.status);
        if (registerRes.data?.error?.message) {
            console.log('Message:', registerRes.data.error.message);
        }
        // Continue - user may already exist
    }

    // Step 2: Create organization
    // API: PUT /api/v1/org
    console.log('Step 2: Creating organization...');
    const orgRes = await makeRequest('PUT', '/api/v1/org', {
        name: ORG_NAME
    });

    if (orgRes.status === 200) {
        console.log(`Organization '${ORG_NAME}' created!`);
    } else {
        console.log('Organization creation response:', orgRes.status);
        if (orgRes.data?.error?.message) {
            console.log('Message:', orgRes.data.error.message);
        }
    }

    // Step 3: Complete user setup
    // API: PATCH /api/v1/user/me/complete
    console.log('Step 3: Completing user setup...');
    const completeRes = await makeRequest('PATCH', '/api/v1/user/me/complete', {
        organizationName: ORG_NAME,
        jobTitle: JOB_TITLE,
        isTrackingAnonymized: true,
        isMarketingOptedIn: false,
        enableEmailDomainAccess: false
    });

    if (completeRes.status === 200) {
        console.log('User setup complete!');
        const user = completeRes.data?.results;
        if (user) {
            console.log(`Organization UUID: ${user.organizationUuid}`);
        }
    } else {
        console.log('User setup response:', completeRes.status);
        if (completeRes.data?.error?.message) {
            console.log('Message:', completeRes.data.error.message);
        }
    }

    // Step 4: Create project with ClickHouse warehouse
    console.log('Step 4: Checking for existing project...');
    let project = await checkIfProjectExists();
    if (project) {
        console.log(`Project already exists: ${project.name} (${project.projectUuid})`);
    } else {
        console.log('Creating project with ClickHouse warehouse...');
        try {
            const job = await createProject();
            console.log(`Project created! UUID: ${job.projectUuid}`);
            project = await checkIfProjectExists();
        } catch (err) {
            console.log('Project creation failed:', err.message);
            console.log('You may need to create the project manually.');
        }
    }

    // Step 5: Create token and write output for CLI upload
    if (project) {
        try {
            const token = await createPersonalAccessToken();
            await writeBootstrapOutput(project.projectUuid, token);
        } catch (err) {
            console.log('Token creation failed:', err.message);
        }
    }

    console.log('');
    console.log('=== Bootstrap Complete ===');
    console.log(`Lightdash is ready at: ${SITE_URL}`);
    console.log(`Login: ${ADMIN_EMAIL} / ${ADMIN_PASSWORD}`);
    console.log('');
}

main().catch(err => {
    console.error('Bootstrap failed:', err.message);
    process.exit(1);
});
