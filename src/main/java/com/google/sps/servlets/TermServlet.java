// Copyright 2019 Google LLC
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

import com.google.sps.data.Term;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;

/** Servlet that finds out if a school is on the quarter system or semester system. */
@WebServlet("/term")
public class TermServlet extends HttpServlet {

  static final List<String> QTR_SCHOOLS =
      Arrays.asList("caltech", "calpoly", "stanford", "berkeley");

  final String QTR_TERMS[] = {
    "Winter", "Winter", "Winter", "Spring", "Spring", "Spring",
    "Summer", "Summer", "Summer", "Fall", "Fall", "Fall"
  };

  final String SEMESTER_TERMS[] = {
    "Spring", "Spring", "Spring", "Spring", "Spring", "Summer",
    "Summer", "Fall", "Fall", "Fall", "Fall", "Fall"
  };

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Boolean isQuarter = getIsQuarter(request.getParameter("school-name"));
    JSONObject json = new JSONObject();
    json.put("quarter", isQuarter);
    Term currTerm = getCurrTerm(isQuarter);
    List<String> terms = termsToString(getTerms(currTerm, /* maxTermOffset */ 2));
    json.put("terms", terms);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  /* Return term system of a school. */
  public static Boolean getIsQuarter(String schoolName) {
    return QTR_SCHOOLS.contains(schoolName);
  }

  /* Get term that corresponds with the current date. */
  private Term getCurrTerm(Boolean isQuarter) {
    java.util.Date date = new Date(); // Initializes to current date.
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    int month = cal.get(Calendar.MONTH);
    String season;
    if (isQuarter) {
      season = QTR_TERMS[month];
    } else {
      season = SEMESTER_TERMS[month];
    }
    int year = cal.get(Calendar.YEAR);
    return new Term(season + " " + String.valueOf(year), isQuarter);
  }

  /* Go num terms forwards and backwards. */
  private List<Term> getTerms(Term currTerm, int maxTermOffset) {
    List<Term> terms = new ArrayList<>();
    terms.add(currTerm);
    Term nextTerm = new Term(currTerm.toString(), currTerm.isQuarter());
    Term prevTerm = new Term(currTerm.toString(), currTerm.isQuarter());
    for (int i = 0; i < maxTermOffset; i++) {
      prevTerm = prevTerm.getPrev();
      nextTerm = nextTerm.getNext();
      terms.add(prevTerm);
      terms.add(nextTerm);
    }
    Collections.sort(terms);
    return terms;
  }

  private List<String> termsToString(List<Term> terms) {
    List<String> str = new ArrayList<>();
    for (Term term : terms) {
      str.add(term.toString());
    }
    return str;
  }
}
