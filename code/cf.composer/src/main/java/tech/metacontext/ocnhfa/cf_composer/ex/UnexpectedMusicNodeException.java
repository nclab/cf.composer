/*
 * Copyright 2019 Jonathan Chang, Chun-yien <ccy@musicapoetica.org>.
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
package tech.metacontext.ocnhfa.cf_composer.ex;

import tech.metacontext.ocnhfa.cf_composer.model.x.MusicNode;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class UnexpectedMusicNodeException extends RuntimeException {

    /**
     * Creates a new instance of <code>UnexpectedMusicNodeException</code>
     * without detail message.
     */
    public UnexpectedMusicNodeException() {

    }

    /**
     * Constructs an instance of <code>UnexpectedMusicNodeException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public UnexpectedMusicNodeException(String msg) {

        super(msg);
    }

    /**
     * Constructs an instance of <code>UnexpectedMusicNodeException</code> with
     * the specified detail message.
     *
     * @param node1
     * @param node2
     */
    public UnexpectedMusicNodeException(MusicNode node1, MusicNode node2) {

        super("MusicNodes = " + node1.getName() + ", " + node2.getName());
    }
}
