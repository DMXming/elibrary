INSERT INTO patron (name, email) VALUES
  ('Alice Chen', 'alice@example.com'),
  ('Bob Wang', 'bob@example.com'),
  ('Carol Lin', 'carol@example.com');

INSERT INTO book (title, author, isbn, publisher, published_date, category, description, total_copies, available_copies, version) VALUES
  ('Clean Code', 'Robert C. Martin', '978-0132350884', 'Prentice Hall', DATE '2008-08-11', 'Software Engineering', 'A handbook of agile software craftsmanship.', 2, 2, 0),
  ('The Pragmatic Programmer', 'Andrew Hunt', '978-0201616224', 'Addison-Wesley', DATE '1999-10-30', 'Software Engineering', 'From journeyman to master.', 3, 3, 0),
  ('Designing Data-Intensive Applications', 'Martin Kleppmann', '978-1449373320', 'O''Reilly Media', DATE '2017-03-16', 'Data Engineering', 'The big ideas behind reliable, scalable, and maintainable systems.', 1, 1, 0),
  ('Domain-Driven Design', 'Eric Evans', '978-0321125217', 'Addison-Wesley', DATE '2003-08-30', 'Software Design', 'Tackling complexity in the heart of software.', 2, 2, 0),
  ('Refactoring', 'Martin Fowler', '978-0134757599', 'Addison-Wesley', DATE '2018-11-30', 'Software Engineering', 'Improving the design of existing code.', 3, 3, 0),
  ('Effective Java', 'Joshua Bloch', '978-0134685991', 'Addison-Wesley', DATE '2018-01-06', 'Java', 'Best practices for the Java platform.', 2, 2, 0),
  ('Java Concurrency in Practice', 'Brian Goetz', '978-0321349606', 'Addison-Wesley', DATE '2006-05-19', 'Java', 'Concurrency and thread safety in Java.', 1, 1, 0),
  ('The Mythical Man-Month', 'Frederick P. Brooks Jr.', '978-0201835953', 'Addison-Wesley', DATE '1995-08-12', 'Project Management', 'Essays on software engineering.', 2, 2, 0),
  ('Introduction to Algorithms', 'Thomas H. Cormen', '978-0262033848', 'MIT Press', DATE '2009-07-31', 'Algorithms', 'A comprehensive introduction to algorithms.', 1, 1, 0),
  ('Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', '978-0062316097', 'Harper', DATE '2015-02-10', 'History', 'A history of the human species.', 3, 3, 0);
