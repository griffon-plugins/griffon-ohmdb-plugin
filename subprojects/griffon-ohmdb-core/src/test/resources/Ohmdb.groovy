/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2014-2020 The author and/or original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
            name = '${application_name}-dev.bin'
        }
    }
    test {
        dataSource {
            name = '${application_name}-test.bin'
        }
    }
    production {
        dataSource {
            name = '${application_name}-prod.bin'
        }
    }
}

dataSources {
    internal {
        name = '${application_name}-internal.bin'
        delete = true
    }
    people {
        name = '${application_name}-people.bin'
        delete = true
    }
}