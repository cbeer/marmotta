/**
 * Copyright (C) 2013 Salzburg Research.
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
package org.apache.marmotta.kiwi.reasoner.model.program;

/**
 * Base class for query filters. Query filters are used to further refine the query results, e.g.
 * by adding a condition on bindings of a variable (AGE < 35).
 * <p/>
 * Currently, the LMF does not evaluate any filters.
 * <p/>
 * User: sschaffe
 */
public interface Filter {

}