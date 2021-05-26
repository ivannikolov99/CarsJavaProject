package my.java.cars.project.cars;

public record Car(String brand,String model,int power,int manufactureYear,double price) {
    @Override
    public String toString() {
        return brand + ":"+model+" HorsePowers:"+power+" Year of Manufacture:"
                +manufactureYear+" Price:"+price+"\n";
    }
}
