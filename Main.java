//Организуем гонки:
//        Все участники должны стартовать одновременно, несмотря на то что на подготовку у каждого их них уходит разное время
//        В туннель не может заехать одновременно больше половины участников (условность)
//        Попробуйте всё это синхронизировать.
//        Только после того как все завершат гонку нужно выдать объявление об окончании
//        Можете корректировать классы (в т.ч. конструктор машин) и добавлять объекты классов из пакета util.concurrent


import java.util.concurrent.Semaphore;
import java.util.concurrent.CountDownLatch;


public class Main {

    private static final CountDownLatch START = new CountDownLatch(8);
    private static final int trackLength = 500000;
    private static final boolean[] PARKING_PLACES = new boolean[5];
    private static final Semaphore SEMAPHORE = new Semaphore(5, true);
    public static void main(String[] args) throws InterruptedException {

        System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Подготовка!!!");

        for (int i = 1; i <= 5; i++) {
            new Thread(new Car(i, (int) (Math.random() * 100 + 50))).start();
            Thread.sleep(1000);
        }

        while (START.getCount() > 3) //Проверяем, собрались ли все автомобили
            Thread.sleep(100);              //у стартовой прямой. Если нет, ждем 100ms

        Thread.sleep(1000);
        System.out.println("На старт!");
        START.countDown();//Команда дана, уменьшаем счетчик на 1
        Thread.sleep(1000);
        System.out.println("Внимание!");
        START.countDown();//Команда дана, уменьшаем счетчик на 1
        Thread.sleep(1000);
        System.out.println("Марш!");
        START.countDown();

        for (int i = 1; i <= 5; i++) {
            new Thread(new Parking.Car(i)).start();
            Thread.sleep(400);
        }



    }

    public static class Car implements Runnable {
        private int carNumber;
        private int carSpeed;

        public Car(int carNumber, int carSpeed) {
            this.carNumber = carNumber;
            this.carSpeed = carSpeed;
        }

        @Override
        public void run() {
            try {
                System.out.printf("Автомобиль №%d подъехал к стартовой прямой.\n", carNumber);
                //Автомобиль подъехал к стартовой прямой - условие выполнено
                //уменьшаем счетчик на 1
                START.countDown();
                //метод await() блокирует поток, вызвавший его, до тех пор, пока
                //счетчик CountDownLatch не станет равен 0
                START.await();
                Thread.sleep(trackLength / carSpeed);//ждем пока проедет трассу
                System.out.printf("Автомобиль №%d финишировал!\n", carNumber);
            } catch (InterruptedException e) {
            }
        }
    }

    public static class Parking {
        
        public static class Car implements Runnable {
            private int carNumber;

            public Car(int carNumber) {
                this.carNumber = carNumber;
            }

            @Override
            public void run() {
                System.out.printf("Автомобиль №%d подъехал к тунелю.\n", carNumber);
                try {

                    SEMAPHORE.acquire();

                    int parkingNumber = -1;


                    synchronized (PARKING_PLACES){
                        for (int i = 0; i < 5; i++)
                            if (!PARKING_PLACES[i]) {
                                PARKING_PLACES[i] = true;
                                parkingNumber = i;
                                System.out.printf("Автомобиль №%d въехал в тунель %d.\n", carNumber, i);
                                break;
                            }
                    }

                    Thread.sleep(5000);

                    synchronized (PARKING_PLACES) {
                        PARKING_PLACES[parkingNumber] = false;
                    }

                    SEMAPHORE.release();
                    System.out.printf("Автомобиль №%d покинул тунель.\n", carNumber);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}

