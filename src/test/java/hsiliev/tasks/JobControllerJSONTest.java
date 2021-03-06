package hsiliev.tasks;

import hsiliev.tasks.dependencies.DependencyCycleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class JobControllerJSONTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  public void emptyBodyShouldError() throws Exception {
    this.mockMvc.perform(
      post("/jobs").contentType(MediaType.APPLICATION_JSON)
    ).andDo(print()).andExpect(
      status().is4xxClientError()
    );
  }

  @Test
  public void correctBodyShouldReturnOrderedTasks() throws Exception {
    this.mockMvc.perform(
      post("/jobs").contentType(MediaType.APPLICATION_JSON).content(TestUtils.readResource("correctRequest.json"))
    ).andDo(print()).andExpect(
      status().isOk()
    ).andExpect(
      content().string(equalToCompressingWhiteSpace(TestUtils.readResource("correctJSONResponse.json")))
    );
  }

  @Test
  public void cycleInTasksShouldError() throws Exception {
    this.mockMvc.perform(
      post("/jobs").contentType(MediaType.APPLICATION_JSON).content(TestUtils.readResource("cycleInTasksRequest.json"))
    ).andDo(print()).andExpect(
      status().is4xxClientError()
    ).andExpect(
      result -> assertTrue(result.getResolvedException() instanceof DependencyCycleException)
    ).andExpect(
      result -> assertThat(result.getResolvedException().getMessage(), containsString("Job contains cycles"))
    );
  }

  @Test
  public void selfLoopInTasksShouldError() throws Exception {
    this.mockMvc.perform(
      post("/jobs").contentType(MediaType.APPLICATION_JSON).content(TestUtils.readResource("selfLoopTaskRequest.json"))
    ).andDo(print()).andExpect(
      status().is4xxClientError()
    ).andExpect(
      result -> assertTrue(result.getResolvedException() instanceof DependencyCycleException)
    ).andExpect(
      result -> assertThat(result.getResolvedException().getMessage(), containsString("Job contains looping task"))
    );
  }
}