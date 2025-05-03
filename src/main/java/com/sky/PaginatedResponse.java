package com.sky;

import java.util.List;

public record PaginatedResponse <T> (
		List<T> items,
		int page,
		int size,
		long totalElements,
		int totalPages
) {
}
