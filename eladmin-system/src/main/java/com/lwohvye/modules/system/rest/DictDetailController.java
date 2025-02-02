/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.lwohvye.modules.system.rest;

import com.lwohvye.annotation.log.Log;
import com.lwohvye.base.BaseEntity.Update;
import com.lwohvye.exception.BadRequestException;
import com.lwohvye.modules.system.domain.DictDetail;
import com.lwohvye.modules.system.service.IDictDetailService;
import com.lwohvye.modules.system.service.dto.DictDetailDto;
import com.lwohvye.modules.system.service.dto.DictDetailQueryCriteria;
import com.lwohvye.utils.result.ResultInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Zheng Jie
 * @date 2019-04-10
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "DictDetailController", description = "系统：字典详情管理")
@RequestMapping("/api/sys/dictDetail")
public class DictDetailController {

    private final IDictDetailService dictDetailService;
    private static final String ENTITY_NAME = "dictDetail";

    @Operation(summary = "查询字典详情")
    @GetMapping
    public ResponseEntity<Object> query(DictDetailQueryCriteria criteria,
                                        @PageableDefault(sort = {"dictSort"}, direction = Sort.Direction.ASC) Pageable pageable) {
        return new ResponseEntity<>(ResultInfo.success(dictDetailService.queryAll(criteria, pageable)), HttpStatus.OK);
    }

    @Operation(summary = "查询多个字典详情")
    @GetMapping(value = "/map")
    public ResponseEntity<Object> getDictDetailMaps(@RequestParam String dictName) {
        String[] names = dictName.split("[,，]");
        Map<String, List<DictDetailDto>> dictMap = new HashMap<>(16);
        for (String name : names) {
            dictMap.put(name, dictDetailService.getDictByName(name));
        }
        return new ResponseEntity<>(ResultInfo.success(dictMap), HttpStatus.OK);
    }

    @Log("新增字典详情")
    @Operation(summary = "新增字典详情")
    @PostMapping
    public ResponseEntity<Object> create(@Validated @RequestBody DictDetail resources) {
        if (resources.getId() != null) {
            throw new BadRequestException("A new " + ENTITY_NAME + " cannot already have an ID");
        }
        dictDetailService.create(resources);
        return new ResponseEntity<>(ResultInfo.success(), HttpStatus.CREATED);
    }

    @Log("修改字典详情")
    @Operation(summary = "修改字典详情")
    @PutMapping
    public ResponseEntity<Object> update(@Validated(Update.class) @RequestBody DictDetail resources) {
        dictDetailService.update(resources);
        return new ResponseEntity<>(ResultInfo.success(), HttpStatus.NO_CONTENT);
    }

    @Log("删除字典详情")
    @Operation(summary = "删除字典详情")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        dictDetailService.delete(id);
        return new ResponseEntity<>(ResultInfo.success(), HttpStatus.OK);
    }
}
