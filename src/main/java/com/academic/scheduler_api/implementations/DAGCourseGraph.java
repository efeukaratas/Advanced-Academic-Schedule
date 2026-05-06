package com.academic.scheduler_api.implementations;

import java.util.ArrayList;
import java.util.List;

public class DAGCourseGraph {

    private List<Integer>[] adjList;
    private int[]           inDegree;
    private int             numCourses;

    @SuppressWarnings("unchecked")
    public void initialize(int numCourses) {
        this.numCourses = numCourses;
        this.inDegree   = new int[numCourses];
        this.adjList    = new ArrayList[numCourses];

        for (int i = 0; i < numCourses; i++)
            adjList[i] = new ArrayList<>();
    }

    // "from tamamlanmadan to başlayamaz"
    public void addPrerequisite(int from, int to) {
        adjList[from].add(to);
        inDegree[to]++;
    }

    public List<Integer> getNeighbors(int courseId) {
        return adjList[courseId];
    }

    public int[] getInDegree()   { return inDegree; }
    public int getNumCourses()   { return numCourses; }
}