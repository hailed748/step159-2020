// // // Copyright 2019 Google LLC
// // //
// // Licensed under the Apache License, Version 2.0 (the "License");
// // you may not use this file except in compliance with the License.
// // You may obtain a copy of the License at
// //
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.TermDataHolder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public final class LiveCourseDataTest {
  private static final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private AddSchoolData schoolData;
  private LiveCourseData liveCourseData;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    helper.setUp();
    schoolData = new AddSchoolData();
    liveCourseData = new LiveCourseData();
  }

  @AfterEach
  public void tearDown() {
    helper.tearDown();
  }

  @Mock HttpServletRequest request;
  @Mock HttpServletRequest requestB;

  @Test
  public void GettingRatingData_AllProperties() {
    DatastoreService db = DatastoreServiceFactory.getDatastoreService();
    createRequest(
        /* requestServelt */ request,
        /* schoolName */ "MIT",
        /* courseName */ "6.006",
        /* termName */ "Spring 2020",
        /* units */ "12",
        /* profName */ "Jason Ku");
    schoolData.addSchoolData(db, request);
    Key parent = findQueryMatch(db, "Term", "term", "Spring 2020").get(0).getKey();
    String parentStr = KeyFactory.keyToString(parent);
    addRatingEntity(
        /* database */ db,
        /* hours */ 12,
        /* difficulty */ 7,
        /* termPerception */ 8,
        /* professorPerception */ 9,
        /* termScore */ 0.82,
        /* profScore */ 0.8,
        /* termComment */ "Great",
        /* professorComment */ "Terrible",
        /* parentEntity */ parent);

    createRequest(/* requestServelt */ requestB, /* term-key */ parentStr);

    List<Object> expectedHoursList = new ArrayList(Arrays.asList(Arrays.asList((long) 12)));
    List<Object> expectedDifficultyList = new ArrayList(Arrays.asList(Arrays.asList((long) 7)));
    List<Object> expectedTermScoreList = new ArrayList(Arrays.asList(Arrays.asList((long) 8)));
    List<Object> expectedProfessorScoreList = new ArrayList(Arrays.asList(Arrays.asList((long) 9)));
    List<Object> expectedTermPerceptionList =
        new ArrayList(Arrays.asList(Arrays.asList((double) 0.82)));
    List<Object> expectedProfessorPerceptionList =
        new ArrayList(Arrays.asList(Arrays.asList((double) 0.8)));
    List<Object> expectedTermCommentsList = new ArrayList(Arrays.asList(Arrays.asList("Terrible")));
    List<Object> expectedProfessorCommentsList =
        new ArrayList(Arrays.asList(Arrays.asList("Great")));

    TermDataHolder answer = liveCourseData.getAllDataFromTerm(db, requestB);

    assertEquals(expectedHoursList, answer.getHoursList());
    assertEquals(expectedDifficultyList, answer.getDifficultyList());
    assertEquals(expectedTermScoreList, answer.getTermScoreList());
    assertEquals(expectedProfessorScoreList, answer.getProfessorScoreList());
    assertEquals(expectedTermPerceptionList, answer.getTermPerceptionList());
    assertEquals(expectedProfessorPerceptionList, answer.getProfessorPerceptionList());
    assertEquals(expectedTermCommentsList, answer.getTermCommentsList());
    assertEquals(expectedProfessorCommentsList, answer.getProfessorCommentsList());
  }

  private void createRequest(
      HttpServletRequest request,
      String schoolName,
      String courseName,
      String termName,
      String units,
      String profName) {
    when(request.getParameter("school-name")).thenReturn(schoolName);
    when(request.getParameter("course-name")).thenReturn(courseName);
    when(request.getParameter("term")).thenReturn(termName);
    when(request.getParameter("num-units")).thenReturn(units);
    when(request.getParameter("prof-name")).thenReturn(profName);
    when(request.getParameter("num-enrolled")).thenReturn("300");
  }

  private void createRequest(HttpServletRequest request, String termKeyStr) {
    when(request.getParameter("term-key")).thenReturn(termKeyStr);
  }

  private List<Entity> findQueryMatch(
      DatastoreService db, String entityType, String entityProperty, String propertyValue) {
    Filter filter = new FilterPredicate(entityProperty, FilterOperator.EQUAL, propertyValue);
    Query q = new Query(entityType).setFilter(filter);
    List<Entity> result = db.prepare(q).asList(FetchOptions.Builder.withDefaults());
    return result;
  }

  private void addRatingEntity(
      DatastoreService db,
      int hours,
      int difficulty,
      int termPerception,
      int profPerception,
      double termScore,
      double profScore,
      String profComments,
      String termComments,
      Key parent) {
    Entity entity = new Entity("Rating", parent);
    entity.setProperty("hours", hours);
    entity.setProperty("difficulty", difficulty);
    entity.setProperty("score-term", termScore);
    entity.setProperty("score-professor", profScore);
    entity.setProperty("perception-term", termPerception);
    entity.setProperty("perception-professor", profPerception);
    entity.setProperty("comments-term", termComments);
    entity.setProperty("comments-professor", profComments);
    db.put(entity);
  }

  private Entity addTermEntity(DatastoreService db) {
    Entity entity = new Entity("Term");
    db.put(entity);
    return entity;
  }
}
