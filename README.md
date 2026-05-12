# 🎓 Advanced Academic Schedule (Gelişmiş Akademik Planlayıcı)

Bu proje, üniversitelerdeki karmaşık ders programı hazırlama sürecini (ders önkoşulları, eğitmen çakışmaları ve sınıf kapasiteleri) modern veri yapıları ve algoritmalar kullanarak otomatik olarak çözen **tam yığın (full-stack)** bir web uygulamasıdır.

## 🚀 Öne Çıkan Özellikler ve Algoritmalar

Bu sistem, basit bir kayıt defterinden ziyade ileri seviye algoritmalarla güçlendirilmiş bir kısıt çözücü (constraint solver) motoruna sahiptir:

* **Graf Modellemesi (DAG):** Dersler ve birbirlerine olan önkoşul bağımlılıkları Yönlü Döngüsüz Graf (Directed Acyclic Graph) olarak modellenmiştir. Bellek optimizasyonu için primitif diziler (Array List) kullanılmıştır.
* **Kahn Algoritması ve Öncelikli Kuyruk (Priority Queue):** Topolojik sıralama (Topological Sort) yapılırken, mezuniyeti en çok riske atan (en uzun önkoşul zincirine sahip) dersler **Kritik Yol (Critical Path)** hesaplaması ile tespit edilir ve `Priority Queue` kullanılarak atamalarda önceliklendirilir.
* **Kısıt Sağlama ve Backtracking:** Eğitmen müsaitliği ve sınıf kapasiteleri kontrol edilir. Sistem tıkanırsa (çözümsüzlük durumu), **Geri İzleme (Backtracking)** algoritması devreye girerek önceki atamaları esnetir ve optimum programı saniyeler içinde bulur.
* **Kullanıcı Dostu Dashboard:** React ile geliştirilen hızlı ve modern arayüz sayesinde kullanıcılar; dersleri, eğitmenleri ve odaları kolayca yönetebilir ve algoritmanın ürettiği sonuçları görsel olarak inceleyebilir.

## 💻 Kullanılan Teknolojiler

**Backend (Çekirdek Motor):**

* Java 17
* Spring Boot
* Spring Data JPA / Hibernate

**Veritabanı:**

* H2 Database (Yüksek hızlı okuma/yazma ve bellek içi performans için)

**Frontend (Kullanıcı Arayüzü):**

* React.js
* Vite (Hızlı derleme ve geliştirme ortamı)
* Saf CSS (Özelleştirilmiş UI bileşenleri)

## 🛠️ Kurulum ve Çalıştırma

Projeyi yerel bilgisayarınızda çalıştırmak için aşağıdaki adımları izleyebilirsiniz.

### 1. Projeyi Klonlayın

```bash
git clone https://github.com/kullaniciadiniz/advanced-academic-schedule.git
cd advanced-academic-schedule

```

### 2. Backend'i Çalıştırın (Spring Boot)

Bilgisayarınızda Java 17 ve Maven kurulu olmalıdır. Kök dizindeyken:

```bash
# Windows için
mvnw.cmd spring-boot:run

# Mac/Linux için
./mvnw spring-boot:run

```

*Backend varsayılan olarak `http://localhost:8080` portunda çalışacaktır.*

### 3. Frontend'i Çalıştırın (React & Vite)

Yeni bir terminal penceresi açın ve `scheduler-ui` klasörüne gidin. Bilgisayarınızda Node.js kurulu olmalıdır.

```bash
cd scheduler-ui
npm install
npm run dev

```

*Frontend varsayılan olarak terminalde belirtilen portta (genellikle `http://localhost:5173`) açılacaktır.*

## 📋 Nasıl Kullanılır?

1. Arayüz üzerinden **Departments (Bölümler)**, **Instructors (Eğitmenler)**, **Rooms (Sınıflar)** ve **Courses (Dersler)** ekleyin.
2. Derslerin birbirleriyle olan bağımlılıklarını (Prerequisites) belirleyin.
3. **Schedule (Planla)** butonuna basın.
4. Gelişmiş arka plan algoritmamız çakışmaları hesaplayacak, en uygun sınıfları ve saatleri atayarak programı **Scheduled Courses** sekmesinde size sunacaktır.
5. Kapasite yetersizliği veya hoca çakışması gibi fiziksel olarak imkansız olan atamalar, sebepleriyle birlikte **Unscheduled Courses** sekmesinde raporlanır.


*Bu proje, Veri Yapıları ve Algoritmalar dersi kapsamında geliştirilmiştir.*
