/**
 * @type {import('semantic-release').GlobalConfig}
 */
export default {
    branches: [
        { name: 'main' },
        { name: 'next', channel: 'next', prerelease: true },
    ],
    plugins: [
        [ '@semantic-release/commit-analyzer' ],
        [ '@semantic-release/github' ],
        [ '@semantic-release/changelog' ],
        [ '@semantic-release/release-notes-generator', {
            presetConfig: {
                types: [
                    { type: 'fix', section: 'Fixes', hidden: false },
                    { type: 'chore', section: 'Chores', hidden: false },
                    { type: 'feat', section: 'Features', hidden: false },
                    { type: 'docs', section: 'Documentation', hidden: true },
                    { type: 'build', section: 'Build', hidden: true },
                    { type: 'ci', section: 'CI', hidden: true },
                    { type: 'perf', section: 'Performance Improvements', hidden: true },
                    { type: 'refactor', section: 'Refactoring', hidden: true },
                    { type: 'test', section: 'Tests', hidden: true },
                ]
            }
        } ],
        [ '@semantic-release/git', {
            assets: [
                'yarn.lock',
                'pom.xml',
                '**/pom.xml',
                'CHANGELOG.md'
            ],
        } ],
        [ 'semantic-release-maven', {
            serverId: 'https://maven.pkg.github.com/NatalieDessi/quarkus-class-file-api-extension'
        } ]
    ]
};