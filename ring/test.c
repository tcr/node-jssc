/* CIRCULAR _ringbuf_buffer QUEUE */
 
#include <stdio.h>
#include <stdint.h>
 
#define RINGBUF_SIZE 6
 
char _ringbuf_buffer[RINGBUF_SIZE];
int _ringbuf_written = 0;      // number of chars in _ringbuf_buffer
int _ringbuf_idx_read = 0;   // indice number of last read char
int _ringbuf_idx_write = 0;  // indice number of last written char
 
// adds a char
int ringbuf_write(uint8_t c)
{
    if (ringbuf_full()) {
        printf("_ringbuf_buffer is full.\n");
        return -1;
    }

    // increase _ringbuf_idx_write, check if at end of array
    if (++_ringbuf_idx_write >= RINGBUF_SIZE) _ringbuf_idx_write = 0;

    _ringbuf_buffer[_ringbuf_idx_write] = c;    
    _ringbuf_written++;
}

int ringbuf_free ()
{
    return RINGBUF_SIZE - _ringbuf_written;
}

int ringbuf_available ()
{
    return _ringbuf_written;
}
 
// returns 1 if _ringbuf_buffer is full, 0 if _ringbuf_buffer is not full
int ringbuf_full(void) 
{
    return _ringbuf_idx_read == _ringbuf_idx_write && _ringbuf_written == RINGBUF_SIZE;
}

// returns 1 if _ringbuf_buffer is empty, 0 if _ringbuf_buffer is not empty
int ringbuf_empty(void)
{
    return _ringbuf_idx_read == _ringbuf_idx_write && _ringbuf_written == 0;
}
 
// pull char from queue
uint8_t ringbuf_read(void) 
{  
  if (ringbuf_empty()) {
    printf("_ringbuf_buffer is empty.\n");
    return -1;
  }

  if (++_ringbuf_idx_read >= RINGBUF_SIZE) _ringbuf_idx_read = 0;
 
 uint8_t b = _ringbuf_buffer[_ringbuf_idx_read];
  printf("\nPopped char %c", b);
 
  // enter space on place of read char so we can see it is removed
  _ringbuf_buffer[_ringbuf_idx_read] = 0x20; 
  _ringbuf_written--;  
  return b;
}
 
// prototypes
int ringbuf_full (void);
int ringbuf_empty (void);
int ringbuf_write (uint8_t c);
uint8_t ringbuf_read (void);
 
int main(void)
{
 
  int i, input; 
  char add;
 
  printf("Circular _ringbuf_buffer Queue Implementation");  
 
  // make sure there are no random chars in array, all spaces
  for (i = 0; i < RINGBUF_SIZE; i++) _ringbuf_buffer[i] = 0x20;
 
  while (input != 4) {

      printf("\n| Written: %d Free: %d\n|  _ringbuf_idx_read: %d write_counter: %d", 
      ringbuf_available(), ringbuf_free(), _ringbuf_idx_read, _ringbuf_idx_write);
 
      printf("\n| Queue content: ");
      for (i = 0; i < RINGBUF_SIZE; i++) printf("[%c]", _ringbuf_buffer[i]);
        printf("\n");
 
    printf("\n    press 1 to push char");
    printf("\n    press 2 to pop char");
    printf("\n    press 4 to exit\n");
    scanf("%d", &input);
 
    // push char
    if (input == 1) {
 
      printf("Enter char: ");
      scanf("%c", &add);
      scanf("%c", &add); // twice otherwise it will get the last enter as input
 
      if (! ringbuf_full())
        ringbuf_write(add);
      else
        printf("\n_ringbuf_buffer IS FULL!");      
 
    }
    // pull char
    else if (input == 2) {
 
      if (! ringbuf_empty())
        ringbuf_read();
      else
        printf("\n_ringbuf_buffer IS EMPTY!");      
    }
 
    printf("\n----");       
  } 
 
  return 0;
}