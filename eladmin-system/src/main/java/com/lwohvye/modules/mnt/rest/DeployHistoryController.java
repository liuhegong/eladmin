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
package com.lwohvye.modules.mnt.rest;

import com.lwohvye.annotation.log.Log;
import com.lwohvye.modules.mnt.service.dto.DeployHistoryQueryCriteria;
import com.lwohvye.modules.mnt.service.IDeployHistoryService;
import com.lwohvye.utils.result.ResultInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * @author zhanghouying
 * @date 2019-08-24
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "DeployHistoryController", description = "运维：部署历史管理")
@RequestMapping("/api/deployHistory")
public class DeployHistoryController {

    private final IDeployHistoryService deployHistoryService;

    @Operation(summary = "导出部署历史数据")
    @GetMapping(value = "/download")
    public void download(HttpServletResponse response, DeployHistoryQueryCriteria criteria) throws IOException {
        deployHistoryService.download(deployHistoryService.queryAll(criteria), response);
    }

    @Operation(summary = "查询部署历史")
    @GetMapping
    public ResponseEntity<Object> query(DeployHistoryQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(ResultInfo.success(deployHistoryService.queryAll(criteria, pageable)), HttpStatus.OK);
    }

    @Log("删除DeployHistory")
    @Operation(summary = "删除部署历史")
    @DeleteMapping
    public ResponseEntity<Object> delete(@RequestBody Set<String> ids) {
        deployHistoryService.delete(ids);
        return new ResponseEntity<>(ResultInfo.success(), HttpStatus.OK);
    }
}
