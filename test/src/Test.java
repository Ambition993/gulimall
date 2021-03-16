public class Test {
    public static void main(String[] args) {
        Animal animal = null;
        Dog dog = new Dog();
        animal = dog;
        System.out.println(dog.seeM()+"+++++++++++++"+animal.SeeM()+"+++++++++++++++"+animal.getM());
    }
}

class Animal {
    int m = 100;

    public int SeeM() {
        return m;
    }

    public int getM() {
        return m;
    }
}

class Dog extends Animal {
    int m = 6;

    public int seeM() {
        return m;
    }
}
