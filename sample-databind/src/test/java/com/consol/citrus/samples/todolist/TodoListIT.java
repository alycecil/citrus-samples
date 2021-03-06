/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.samples.todolist;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.samples.todolist.model.TodoEntry;
import com.consol.citrus.validation.json.JsonMappingValidationCallback;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.UUID;

/**
 * @author Christoph Deppisch
 */
public class TodoListIT extends TestNGCitrusTestDesigner {

    @Autowired
    private HttpClient todoClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @CitrusTest
    public void testObjectMapping() {
        final UUID uuid = UUID.randomUUID();
        variable("todoId", uuid.toString());
        variable("todoName", "citrus:concat('todo_', citrus:randomNumber(4))");
        variable("todoDescription", "Description: ${todoName}");

        http()
            .client(todoClient)
            .send()
            .post("/api/todolist")
            .contentType("application/json")
            .payload(new TodoEntry(uuid, "${todoName}", "${todoDescription}"), objectMapper);

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.PLAINTEXT)
            .payload("${todoId}");

        http()
            .client(todoClient)
            .send()
            .get("/api/todo/${todoId}")
            .accept("application/json");

        http()
            .client(todoClient)
            .receive()
            .response(HttpStatus.OK)
            .validationCallback(new JsonMappingValidationCallback<TodoEntry>(TodoEntry.class, objectMapper) {
                @Override
                public void validate(TodoEntry todoEntry, Map<String, Object> headers, TestContext context) {
                    Assert.assertNotNull(todoEntry);
                    Assert.assertEquals(todoEntry.getId(), uuid);
                }
            });
    }

}
