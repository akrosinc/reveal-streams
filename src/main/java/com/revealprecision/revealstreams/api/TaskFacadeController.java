package com.revealprecision.revealstreams.api;


import com.revealprecision.revealstreams.dto.TaskFacade;
import com.revealprecision.revealstreams.dto.TaskSyncRequest;
import com.revealprecision.revealstreams.service.TaskFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.UUID;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/v2/task")
@Slf4j
public class TaskFacadeController {

  public static final String TOTAL_RECORDS = "total_records";
  public final TaskFacadeService taskFacadeService;

  @Autowired
  public TaskFacadeController(TaskFacadeService taskFacadeService) {
    this.taskFacadeService = taskFacadeService;
  }

  @Operation(summary = "Facade for Android Task Resource", description = "Sync Tasks", tags = {
      "Task-Facade"})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/sync", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public ResponseEntity<List<TaskFacade>> taskSync(@RequestBody TaskSyncRequest taskSyncRequest) {

    List<UUID> jurisdictionIdentifiers = taskSyncRequest.getGroup();
    boolean returnCount = taskSyncRequest.isReturnCount();
    Long serverVersion = taskSyncRequest.getServerVersion();
    if (serverVersion == null) {
      serverVersion = 0L;
    }

    List<TaskFacade> taskFacades = taskFacadeService.syncTasks(taskSyncRequest.getPlan(),
        jurisdictionIdentifiers, serverVersion, "principle");

    if (returnCount) {
      HttpHeaders headers = new HttpHeaders();
      headers.add(TOTAL_RECORDS, String.valueOf(taskFacades.size()));
      return ResponseEntity.ok().headers(headers).body(taskFacades);
    } else {
      return ResponseEntity.ok().body(taskFacades);
    }

  }


}