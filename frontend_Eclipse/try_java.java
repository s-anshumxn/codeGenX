public class StudentManagement {
        private String studentName;
        private int studentAge;
        private String studentAddress;
        private double[] grades;
    
        public StudentManagement(String name, int age, String address, double[] grades) {
            this.studentName = name;
            this.studentAge = age;
            this.studentAddress = address;
            this.grades = grades;
        }
    
        public String getStudentName() {
            return studentName;
        }
    
        public void setStudentName(String name) {
            this.studentName = name;
        }
    
        public int getStudentAge() {
            return studentAge;
        }
    
        public void setStudentAge(int age) {
            this.studentAge = age;
        }
    
        public String getStudentAddress() {
            return studentAddress;
        }
    
        public void setStudentAddress(String address) {
            this.studentAddress = address;
        }
    
        public double[] getGrades() {
            return grades;
        }
    
        public void setGrades(double[] grades) {
            this.grades = grades;
        }
    
        public double calculateAverageGrade() {
            double total = 0;
            for (int i = 0; i < grades.length; i++) {
                total += grades[i];
            }
            return total / grades.length;
        }
    
        public void printStudentDetails() {
            System.out.println("Name: " + studentName);
            System.out.println("Age: " + studentAge);
            System.out.println("Address: " + studentAddress);
            System.out.println("Grades: ");
            for (int i = 0; i < grades.length; i++) {
                System.out.print(grades[i] + " ");
            }
            System.out.println();
            System.out.println("Average Grade: " + calculateAverageGrade());
        }
    
        public static void main(String[] args) {
            double[] grades = { 85.5, 90.0, 78.5, 88.0 };
            StudentManagement student = new StudentManagement("John Doe", 20, "123 Main St", grades);
            student.printStudentDetails();
        }
    }