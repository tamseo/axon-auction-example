/*
 * Copyright (c) 2010. Axon Auction Example
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
package org.fuin.auction.client.test;

import static org.fest.assertions.Assertions.assertThat;

import java.util.UUID;

import org.fuin.auction.command.api.base.AggregateIdentifierResult;
import org.fuin.auction.command.api.base.CreateCategoryCommand;
import org.fuin.auction.command.api.base.DeleteCategoryCommand;
import org.fuin.auction.command.api.base.MarkCategoryForDeletionCommand;
import org.fuin.auction.command.api.base.ResultCode;
import org.fuin.auction.command.api.support.CommandResult;
import org.fuin.auction.query.api.CategoryDto;
import org.junit.Test;

//TESTCODE:BEGIN
public final class CategoryUseCaseTest extends AbstractUseCaseTest {

	@Test
	// TODO michael Refactor this as it's not really a use case
	public final void testCategoryLifeCycle() {

		final String categoryName = UUID.randomUUID().toString();

		// Create category
		CommandResult result = getCommandService().send(new CreateCategoryCommand(categoryName));
		assertSuccess(result, ResultCode.CATEGORY_SUCCESSFULLY_CREATED);
		assertAggregateIdentifierResult(result);

		waitUntilAtLeastOneCategoryIsAvailable();

		final AggregateIdentifierResult aiResult = (AggregateIdentifierResult) result;
		final CategoryDto categoryDto = new CategoryDto(aiResult.getId(), categoryName, true);
		assertThat(getQueryService().findAllCategories()).contains(categoryDto);
		assertThat(getQueryService().findAllActiveCategories()).contains(categoryDto);

		// Mark category for deletion
		result = getCommandService().send(new MarkCategoryForDeletionCommand(categoryDto.getId()));
		assertSuccess(result, ResultCode.CATEGORY_SUCCESSFULLY_MARKED_FOR_DELETION);

		waitUntilNoActiveCategoryIsAvailable();

		assertThat(getQueryService().findAllCategories()).contains(categoryDto);
		assertThat(getQueryService().findAllActiveCategories().size()).isEqualTo(0);

		// Delete category
		result = getCommandService().send(new DeleteCategoryCommand(categoryDto.getId()));
		assertSuccess(result, ResultCode.CATEGORY_SUCCESSFULLY_DELETED);

		waitUntilNoMoreCategoriesAreAvailable();

		assertThat(getQueryService().findAllCategories().size()).isEqualTo(0);
		assertThat(getQueryService().findAllActiveCategories().size()).isEqualTo(0);

	}

	private void waitUntilAtLeastOneCategoryIsAvailable() {
		waitUntil(new Condition() {
			@Override
			public boolean isTrue() {
				return getQueryService().findAllCategories().size() > 0;
			}
		}, 5);
	}

	private void waitUntilNoMoreCategoriesAreAvailable() {
		waitUntil(new Condition() {
			@Override
			public boolean isTrue() {
				return getQueryService().findAllCategories().size() > 0;
			}
		}, 5);
	}

	private void waitUntilNoActiveCategoryIsAvailable() {
		waitUntil(new Condition() {
			@Override
			public boolean isTrue() {
				return getQueryService().findAllActiveCategories().size() == 0;
			}
		}, 5);
	}

}
// TESTCODE:END
