#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <ctype.h>
#include <unistd.h>

#include <sys/socket.h>
#include <arpa/inet.h>
#include <pthread.h>

#define BUFFER_SIZE 1024

void fatal ( char *comment ) {
  fprintf(stderr,"Error: %s\n",comment);
  perror("perror() returns:");
  exit(-1);
}

void toLowerCase ( char *s ) {
  for (int i=0; s[i]; i++) s[i] = tolower(s[i]);
}

struct sockaddr *address_of ( char *host, int port ) {
  struct sockaddr_in *addr = (struct sockaddr_in *) malloc(sizeof(struct sockaddr_in));
  memset((char *) addr,0,sizeof(struct sockaddr_in));
  addr->sin_family = AF_INET;
  addr->sin_port = htons(port);
  if (host == NULL)
    addr->sin_addr.s_addr = htonl(INADDR_ANY);
  else {
    int ret = inet_aton(host,&(addr->sin_addr));
    if (ret == 0) fatal("inet_aton() failed to convert IP address of peer");
  }
  return (struct sockaddr *) addr;
}

void *serveClient ( void *argument ) {
  int c_sock = (int) argument;
  char buffer[BUFFER_SIZE];
  while (1) {
    int message_length = read(c_sock,buffer,BUFFER_SIZE-1);
    if (message_length == -1) fatal("read() failed");
    if (message_length == 0) break;
    buffer[message_length] = 0;
    printf("%s\n",buffer);
    toLowerCase(buffer);
    if (strcmp(buffer,"stop") == 0) break;
  }
  close(c_sock);
  return NULL;
}

void server ( int port ) {
  int s = socket(AF_INET,SOCK_STREAM,0);
  if (s == -1) fatal("Unable to create socket");
  struct sockaddr *my_addr = address_of(NULL,port);
  int ret = bind(s ,my_addr, sizeof(struct sockaddr_in));
  free((char *) my_addr);
  if (ret == -1) fatal("Unable to bind socket to given port");
  ret = listen(s,2);
  if (ret == -1) fatal("listen() failed");
  while (1) {
    struct sockaddr client_address;
    socklen_t client_address_length;
    int c_sock = accept(s,&client_address,&client_address_length);
    if (c_sock == -1) fatal("accept() failed");
    pthread_t worker;
    ret = pthread_create(&worker,NULL,&serveClient,(void *) c_sock);
  }
}

void client ( char *host, int port ) {
  int s = socket(AF_INET,SOCK_STREAM,0);
  if (s == -1) fatal("Unable to create socket");
  struct sockaddr *peer = address_of(host,port);
  int ret = connect(s,peer,sizeof(struct sockaddr_in));
  if (ret == -1) fatal("connect() failed");
  char *buffer = malloc(BUFFER_SIZE);
  while (1) {
    size_t bsize = BUFFER_SIZE;
    int n_chars = getline(&buffer,&bsize,stdin);
    if (n_chars == -1) fatal("getline() from stdin failed");
    ret = write(s,buffer,n_chars);
    if (ret == -1) fatal("write() failed");
    toLowerCase(buffer);
    if (strcmp(buffer,"stop") == 0) break;
  }
  close(s);
  free(buffer);
  free((char *) peer);
}

int main ( int ac, char **av ) {
  if (ac != 3) {
    fprintf(stderr,"Usage: \"%s -l <port>\" or \"%s <host> <port>\"\n",av[0],av[0]);
    exit(-1);
  }

  int port = strtol(av[2],NULL,10);
  toLowerCase(av[1]);
  if (strncmp(av[1],"-l",2) == 0)
    server(port);
  else
    client(av[1],port);
}
