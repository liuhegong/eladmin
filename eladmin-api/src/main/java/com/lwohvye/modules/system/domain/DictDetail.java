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
package com.lwohvye.modules.system.domain;

import com.lwohvye.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
* @author Zheng Jie
* @date 2019-04-10
*/
@Entity
@Getter
@Setter
@Accessors(chain = true)
@Table(name="sys_dict_detail")
public class DictDetail extends BaseEntity implements Serializable {

    @Id
    @Column(name = "detail_id")
    @NotNull(groups = Update.class)
    @Schema(description = "ID" , accessMode = Schema.AccessMode.READ_ONLY)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "dict_id")
    @ManyToOne(fetch=FetchType.LAZY)
    @Schema(description = "字典" , accessMode = Schema.AccessMode.READ_ONLY)
    private Dict dict;

    @Schema(description = "字典标签" )
    private String label;

    @Schema(description = "字典值" )
    private String value;

    @Schema(description = "排序" )
    private Integer dictSort = 999;
}
