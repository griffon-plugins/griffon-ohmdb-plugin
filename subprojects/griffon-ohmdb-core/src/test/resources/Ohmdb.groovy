/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
dataSource {
    delete = true
}

environments {
    development {
        dataSource {
            name = '@application.name@-dev.bin'
        }
    }
    test {
        dataSource {
            name = '@application.name@-test.bin'
        }
    }
    production {
        dataSource {
            name = '@application.name@-prod.bin'
        }
    }
}

dataSources {
    internal {
        name = '@application.name@-internal.bin'
        delete = true
    }
    people {
        name = '@application.name@-people.bin'
        delete = true
    }
}