CREATE USER bookingsapp WITH PASSWORD 'verysecretpassword';
GRANT CONNECT ON DATABASE hotelbookings TO bookingsapp;

\c hotelbookings

GRANT USAGE ON SCHEMA public TO bookingsapp;
GRANT CREATE ON SCHEMA public TO bookingsapp;

ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO bookingsapp;
