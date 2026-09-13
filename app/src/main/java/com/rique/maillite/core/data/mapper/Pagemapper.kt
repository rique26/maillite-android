package com.rique.maillite.core.data.mapper

import com.rique.maillite.core.data.remote.dto.PageResponseDto
import com.rique.maillite.core.domain.model.PagedResult

/**
 * Qualquer endpoint paginado novo que aparecer reaproveita isso,
 * só passando a função de mapeamento do item (DTO -> domain model) daquela feature.
 */
fun <DTO, DOMAIN> PageResponseDto<DTO>.toDomain(mapItem: (DTO) -> DOMAIN): PagedResult<DOMAIN> = PagedResult(
    items = content.map(mapItem),
    currentPage = number,
    totalPages = totalPages
)