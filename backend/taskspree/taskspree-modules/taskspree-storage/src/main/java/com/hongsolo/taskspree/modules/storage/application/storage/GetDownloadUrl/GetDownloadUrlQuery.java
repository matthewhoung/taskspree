package com.hongsolo.taskspree.modules.storage.application.storage.GetDownloadUrl;

import com.hongsolo.taskspree.common.application.cqrs.Query;
import com.hongsolo.taskspree.common.domain.Result;

import java.util.UUID;

public record GetDownloadUrlQuery(
        UUID fileId
) implements Query<Result<String>> {
}
