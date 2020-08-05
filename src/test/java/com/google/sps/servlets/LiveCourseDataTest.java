// // // Copyright 2019 Google LLC
// // //
// // Licensed under the Apache License, Version 2.0 (the "License");
// // you may not use this file except in compliance with the License.
// // You may obtain a copy of the License at
// //
// //     https://www.apache.org/licenses/LICENSE-2.0
// //
// // Unless required by applicable law or agreed to in writing, software
// // distributed under the License is distributed on an "AS IS" BASIS,
// // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// // See the License for the specific language governing permissions and
// // limitations under the License.

// package com.google.sps.servlets;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.mockito.Mockito.when;

// import com.google.appengine.api.datastore.DatastoreService;
// import com.google.appengine.api.datastore.DatastoreServiceFactory;
// import com.google.appengine.api.datastore.Entity;
// import com.google.appengine.api.datastore.FetchOptions;
// import com.google.appengine.api.datastore.Key;
// import com.google.appengine.api.datastore.Query;
// import com.google.appengine.api.datastore.Query.Filter;
// import com.google.appengine.api.datastore.Query.FilterOperator;
// import com.google.appengine.api.datastore.Query.FilterPredicate;
// import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
// import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
// import com.google.sps.data.TermDataHolder;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.List;
// import javax.servlet.http.HttpServletRequest;
// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// public final class LiveCourseDataTest {
//   private static final LocalServiceTestHelper helper =
//       new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

//   private AddSchoolData schoolData;
//   private LiveCourseData liveCourseData;

//   @BeforeEach
//   public void setUp() {
//     MockitoAnnotations.initMocks(this);
//     helper.setUp();
//     schoolData = new AddSchoolData();
//     liveCourseData = new LiveCourseData();
//   }

//   @AfterEach
//   public void tearDown() {
//     helper.tearDown();
//   }

//   @Mock HttpServletRequest request;
//   @Mock HttpServletRequest requestB;

//   @Test
//   public void FindingExisitngTermEntity() {
//     DatastoreService db = DatastoreServiceFactory.getDatastoreService();
//     createRequest(
//         request, /*schoolName*/
//         "MIT", /*courseName*/
//         "6.006", /*termName*/
//         "Spring 2020", /*units*/
//         "12", /*profName*/
//         "Jason Ku");
//     createRequest(requestB, "MIT", "6.008", "Spring 2018", "6", "Srini");
//     String expectedTermName = "Spring 2020";

//     schoolData.addSchoolData(db, request);
//     schoolData.addSchoolData(db, requestB);
//     Key expectedParent = findQueryMatch(db, "Course", "course-name", "6.006").get(0).getKey();
//     Entity found = liveCourseData.getTerm(db, request);

//     assertEquals(expectedParent, found.getParent());
//     assertEquals(expectedTermName, found.getProperty("term"));
//   }

//   @Test
//   public void GettingRatingData_AllProperties() {
//     DatastoreService db = DatastoreServiceFactory.getDatastoreService();
//     createRequest(
//         request, /*schoolName*/
//         "MIT", /*courseName*/
//         "6.006", /*termName*/
//         "Spring 2020", /*units*/
//         "12", /*profName*/
//         "Jason Ku");
//     schoolData.addSchoolData(db, request);
//     Key parent = findQueryMatch(db, "Term", "term", "Spring 2020").get(0).getKey();
//     addRatingEntity(db,12, 7, 5, 5, 5, 5, "Great", "Terrible",parent);

//     List<Object> expectedHoursList = new ArrayList(Arrays.asList((long) 12));
//     List<Object> expectedDifficultyList = new ArrayList(Arrays.asList((long) 7));
//     List<Object> expectedTermScoreList = new ArrayList(Arrays.asList((long) 5));
//     List<Object> expecyedProfessorScoreList = new ArrayList(Arrays.asList((long) 5));
//     List<Object> expectedTermPerceptionList = new ArrayList(Arrays.asList((long) 5));
//     List<Object> expectedProfessorPerceptionList = new ArrayList(Arrays.asList((long) 5));
//     List<Object> expectedTermCommentsList = new ArrayList("");

//     List<Object> e = new ArrayList(Arrays.asList("Great"));
//     List<Object> f = new ArrayList(Arrays.asList("Terrible"));

//     TermDataHolder answer = liveCourseData.getAllDataFromTerm(db, request);

//     assertEquals(expectedHoursList, answer.getHoursList());
//     assertEquals(expectedDifficultyList, answer.getDifficultyList());
//     assertEquals(a, answer.getTermScoreList());
//     assertEquals(b, answer.getProfessorScoreList());
//     assertEquals(c, answer.getTermPerceptionList());
//     assertEquals(d, answer.getProfessorPerceptionList());
//     assertEquals(f, answer.getTermCommentsList());
//     assertEquals(e, answer.getProfessorCommentsList());
//   }

//   private void createRequest(
//       HttpServletRequest request,
//       String schoolName,
//       String courseName,
//       String termName,
//       String units,
//       String profName) {
//     when(request.getParameter("school-name")).thenReturn(schoolName);
//     when(request.getParameter("course-name")).thenReturn(courseName);
//     when(request.getParameter("term")).thenReturn(termName);
//     when(request.getParameter("units")).thenReturn(units);
//     when(request.getParameter("professor-name")).thenReturn(profName);
//   }

//   private List<Entity> findQueryMatch(
//       DatastoreService db, String entityType, String entityProperty, String propertyValue) {
//     Filter filter = new FilterPredicate(entityProperty, FilterOperator.EQUAL, propertyValue);
//     Query q = new Query(entityType).setFilter(filter);
//     List<Entity> result = db.prepare(q).asList(FetchOptions.Builder.withDefaults());
//     return result;
//   }

//   private void addRatingEntity(DatastoreService db, int hours, int difficulty, int tScore, int
// pScore, int termPer, int profPer, String profCom, String termCom, Key parent) {
//     Entity entity = new Entity("Rating", parent);
//     entity.setProperty("hours", hours);
//     entity.setProperty("difficulty", difficulty);
//     entity.setProperty("score-term", tScore);
//     entity.setProperty("score-professor", pScore);
//     entity.setProperty("perception-term", termPer);
//     entity.setProperty("perception-professor", profPer);
//     entity.setProperty("comments-term", termCom);
//     entity.setProperty("comments-professor", profCom);
//     db.put(entity);
//   }

//   private Entity addTermEntity(DatastoreService db) {
//     Entity entity = new Entity("Term");
//     db.put(entity);
//     return entity;
//   }
// }
