/*
 * Copyright 2017 Aljoscha Grebe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.almightyalpaca.jetbrains.plugins.discord.presence;

import com.almightyalpaca.jetbrains.plugins.discord.data.FileInfo;
import com.almightyalpaca.jetbrains.plugins.discord.data.InstanceInfo;
import com.almightyalpaca.jetbrains.plugins.discord.data.ProjectInfo;
import com.almightyalpaca.jetbrains.plugins.discord.data.ReplicatedData;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Comparator;

public class PresenceRenderContext implements Serializable
{
    @NotNull
    private static final Gson GSON = new Gson();
    @NotNull
    private static final Logger LOG = LoggerFactory.getLogger(PresenceRenderContext.class);

    @Nullable
    private final InstanceInfo instance;
    @Nullable
    private final ProjectInfo project;
    @Nullable
    private final FileInfo file;

    public PresenceRenderContext(@Nullable InstanceInfo instance, @Nullable ProjectInfo project, @Nullable FileInfo file)
    {
        LOG.trace("PresenceRenderContext#new({}, {}, {})", instance, project, file);

        this.instance = instance;
        this.project = project;
        this.file = file;
    }

    public PresenceRenderContext(@Nullable ReplicatedData data)
    {
        LOG.trace("PresenceRenderContext#new({})", data);


        InstanceInfo instance;
        ProjectInfo project;
        FileInfo file;

        if (data != null)
        {
            // @formatter:off
            instance = data.getInstances().values().stream()
                    .filter(i -> i.getSettings().isEnabled())
                    .max(Comparator.naturalOrder())
                    .orElse(null);

            if (instance != null)
            {
                project = instance.getProjects().values().stream()
                        .filter(p -> p.getSettings().isEnabled())
                        .max(Comparator.naturalOrder())
                        .orElse(null);

                if (project != null)
                {
                    final InstanceInfo instanceFinal = instance;
                    file = project.getFiles().values().stream()
                            .filter(f -> !(instanceFinal.getSettings().isHideReadOnlyFiles() && f.isReadOnly()))
                            .max(Comparator.naturalOrder())
                            .orElse(null);
                }
                else
                {
                    if (!instance.getSettings().isShowIDEWhenNoProjectIsAvailable())
                        instance = null;

                    file = null;
                }
            }
            else
            {
                project = null;
                file = null;
            }
            // @formatter:on
        }
        else
        {
            instance = null;
            project = null;
            file = null;
        }

        this.instance = instance;
        this.project = project;
        this.file = file;

        LOG.trace("PresenceRenderContext#new({}, {}, {})", instance != null ? instance.getDistribution().getCode() : null, project != null ? project.getName() : null, file != null ? file.getName() : null);
    }

    @Nullable
    public InstanceInfo getInstance()
    {
        return this.instance;
    }

    @Nullable
    public ProjectInfo getProject()
    {
        return this.project;
    }

    @Nullable
    public FileInfo getFile()
    {
        return this.file;
    }

    @NotNull
    @Override
    public String toString()
    {
        return GSON.toJson(this);
    }

    public boolean isEmpty()
    {
        return instance == null;
    }
}