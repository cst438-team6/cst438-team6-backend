package com.cst438.domain;

import jakarta.persistence.*;

@Entity
public class Enrollment {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="enrollment_id")
    int enrollmentId;

    private String grade = null;
    @ManyToOne
    @JoinColumn(name="section_no", nullable=false)
    private Section section;
    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    public int getEnrollmentId() {return this.enrollmentId;}
    public void setEnrollmentId(int enrollmentId) {this.enrollmentId = enrollmentId;}
    public String getGrade() {return this.grade;}
    public void setGrade(String grade) {this.grade = grade;}
    public Section getSection() {return this.section;}
    public void setSection(Section section) {this.section = section;}
    public User getUser() {return this.user;}
    public void setUser(User user) {this.user = user;}



	// DONE complete this class
    // add additional attribute for grade
    // create relationship between enrollment and user entities
    // create relationship between enrollment and section entities
    // add getter/setter methods
}
