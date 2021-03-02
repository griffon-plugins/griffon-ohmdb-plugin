/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2014-2021 The author and/or original authors.
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
package griffon.plugins.ohmdb

import com.ohmdb.api.Db
import com.ohmdb.api.Table
import griffon.annotations.inject.BindTo
import griffon.core.GriffonApplication
import griffon.plugins.ohmdb.events.OhmdbConfigurationSetupEvent
import griffon.plugins.ohmdb.events.OhmdbConnectEndEvent
import griffon.plugins.ohmdb.events.OhmdbConnectStartEvent
import griffon.plugins.ohmdb.events.OhmdbDisconnectEndEvent
import griffon.plugins.ohmdb.events.OhmdbDisconnectStartEvent
import griffon.test.core.GriffonUnitRule
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Unroll

import javax.application.event.EventHandler
import javax.inject.Inject

@Unroll
class OhmdbSpec extends Specification {
    static {
        System.setProperty('org.slf4j.simpleLogger.defaultLogLevel', 'trace')
    }

    @Rule
    public final GriffonUnitRule griffon = new GriffonUnitRule()

    @Inject
    private DbHandler dbHandler

    @Inject
    private GriffonApplication application

    void 'Open and close default dataSource'() {
        given:
        List eventNames = [
            'OhmdbConnectStartEvent', 'OhmdbConfigurationSetupEvent', 'OhmdbConnectEndEvent',
            'OhmdbDisconnectStartEvent', 'OhmdbDisconnectEndEvent'
        ]
        TestEventHandler testEventHandler = new TestEventHandler()
        application.eventRouter.subscribe(testEventHandler)

        when:
        dbHandler.withOhmdb { String dataSourceName, Db db ->
            true
        }
        dbHandler.closeOhmdb()
        // second call should be a NOOP
        dbHandler.closeOhmdb()

        then:
        testEventHandler.events.size() == 5
        testEventHandler.events == eventNames
    }

    void 'Connect to default dataSource'() {
        expect:
        dbHandler.withOhmdb { String dataSourceName, Db db ->
            dataSourceName == 'default' && db
        }
    }

    void 'Bootstrap init is called'() {
        given:
        assert !bootstrap.initWitness

        when:
        dbHandler.withOhmdb { String dataSourceName, Db db -> }

        then:
        bootstrap.initWitness
        !bootstrap.destroyWitness
    }

    void 'Bootstrap destroy is called'() {
        given:
        assert !bootstrap.initWitness
        assert !bootstrap.destroyWitness

        when:
        dbHandler.withOhmdb { String dataSourceName, Db db -> }
        dbHandler.closeOhmdb()

        then:
        bootstrap.initWitness
        bootstrap.destroyWitness
    }

    void 'Can connect to #name dataSource'() {
        expect:
        dbHandler.withOhmdb(name) { String dataSourceName, Db db ->
            dataSourceName == name && db
        }

        where:
        name       | _
        'default'  | _
        'internal' | _
        'people'   | _
    }

    void 'Bogus dataSource name (#name) results in error'() {
        when:
        dbHandler.withOhmdb(name) { String dataSourceName, Db db ->
            true
        }

        then:
        thrown(IllegalArgumentException)

        where:
        name    | _
        null    | _
        ''      | _
        'bogus' | _
    }

    void 'Execute statements on people dataSource'() {
        when:
        List peopleIn = dbHandler.withOhmdb('people') { String dataSourceName, Db db ->
            Table<Person> people = db.table(Person)
            [[name: 'Danno', lastname: 'Ferrin'],
             [name: 'Andres', lastname: 'Almiray'],
             [name: 'James', lastname: 'Williams'],
             [name: 'Guillaume', lastname: 'Laforge'],
             [name: 'Jim', lastname: 'Shingler'],
             [name: 'Alexander', lastname: 'Klein'],
             [name: 'Rene', lastname: 'Groeschke']].collect { data ->
                Person person = new Person(data)
                people.insert(person)
                person
            }
        }

        List peopleOut = dbHandler.withOhmdb('people') { String dataSourceName, Db db ->
            Table<Person> people = db.table(Person)
            people.getAll(people.ids()).collect { it }
        }

        then:
        peopleIn == peopleOut

        cleanup:
        dbHandler.closeOhmdb()
    }

    @BindTo(OhmdbBootstrap)
    private TestOhmdbBootstrap bootstrap = new TestOhmdbBootstrap()

    private class TestEventHandler {
        List<String> events = []

        @EventHandler
        void handleOhmdbConnectStartEvent(OhmdbConnectStartEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleOhmdbConfigurationSetupEvent(OhmdbConfigurationSetupEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleOhmdbConnectEndEvent(OhmdbConnectEndEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleOhmdbDisconnectStartEvent(OhmdbDisconnectStartEvent event) {
            events << event.class.simpleName
        }

        @EventHandler
        void handleOhmdbDisconnectEndEvent(OhmdbDisconnectEndEvent event) {
            events << event.class.simpleName
        }
    }
}
