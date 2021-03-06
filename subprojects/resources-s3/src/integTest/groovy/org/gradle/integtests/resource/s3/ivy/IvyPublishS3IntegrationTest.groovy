/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.integtests.resource.s3.ivy

import org.gradle.api.publish.ivy.AbstractIvyPublishIntegTest
import org.gradle.integtests.resource.s3.fixtures.S3FileBackedServer
import org.junit.Rule

class IvyPublishS3IntegrationTest extends AbstractIvyPublishIntegTest {

    String bucket = 'tests3bucket'

    @Rule
    public S3FileBackedServer server = new S3FileBackedServer(file())

    def setup() {
        executer.withArgument("-Dorg.gradle.s3.endpoint=${server.getUri()}")
    }

    def "can publish to a S3 Ivy repository"() {
        given:
        def ivyRepo = ivy(server.getBackingDir(bucket))

        settingsFile << 'rootProject.name = "publishS3Test"'
        buildFile << """
apply plugin: 'java'
apply plugin: 'ivy-publish'

group = 'org.gradle.test'
version = '1.0'

publishing {
    repositories {
        ivy {
            url "${ivyRepo.uri}"
            credentials(AwsCredentials) {
                accessKey "someKey"
                secretKey "someSecret"
            }
        }
    }
    publications {
        ivy(IvyPublication) {
            from components.java
        }
    }
}
"""

        when:
        succeeds 'publish'

        then:
        def module = ivyRepo.module('org.gradle.test', 'publishS3Test', '1.0')
        module.assertPublishedAsJavaModule()
    }
}
