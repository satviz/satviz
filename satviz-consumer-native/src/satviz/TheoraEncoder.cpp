#include <satviz/TheoraEncoder.hpp>

#include <fstream>
#include <theora/theoraenc.h>

namespace satviz {
namespace video {

struct TheoraStream {
  std::ofstream    file;
  ogg_stream_state ogg;
  th_enc_ctx      *enc;

  ~TheoraStream() {
    th_encode_free(enc);
    ogg_stream_clear(&ogg);
  }

  inline void writePage(ogg_page &page) {
    file.write((char *) page.header, page.header_len);
    file.write((char *) page.body,   page.body_len);
  }

  inline void flushPage() {
    ogg_page page;
    while (ogg_stream_flush(&ogg, &page) > 0) {
      writePage(page);
    }
  }
};

TheoraEncoder::TheoraEncoder() {
}

TheoraEncoder::~TheoraEncoder() {
  delete stream;
}

bool TheoraEncoder::startRecording(const char *filename, int w, int h) {
  width  = w;
  height = h;
  stream = new TheoraStream;

  stream->file = std::ofstream(filename, std::ofstream::binary);
  if (!stream->file.is_open()) {
    return false;
  }

  int frame_w, frame_h, pic_x, pic_y;
  /* Theora has a divisible-by-sixteen restriction for the encoded frame size */
  /* scale the picture size up to the nearest /16 and calculate offsets */
  frame_w = (width  + 15) & ~0xF;
  frame_h = (height + 15) & ~0xF;
  /*Force the offsets to be even so that chroma samples line up like we expect.*/
  pic_x = ((frame_w - width)  >> 1) & ~1;
  pic_y = ((frame_h - height) >> 1) & ~1;

  th_info info;
  th_info_init(&info);
  info.frame_width        = frame_w;
  info.frame_height       = frame_h;
  info.pic_width          = width;
  info.pic_height         = height;
  info.pic_x              = pic_x;
  info.pic_y              = pic_y;
  info.colorspace         = TH_CS_UNSPECIFIED;
  info.pixel_fmt          = TH_PF_444;
  info.target_bitrate     = 0;
  info.quality            = 31; /* Between 0 and 63 inclusive */
  info.fps_numerator      = 30;
  info.fps_denominator    = 1;
  info.aspect_numerator   = 1;
  info.aspect_denominator = 1;

  th_comment comment;
  th_comment_init(&comment);

  ogg_stream_init(&stream->ogg, (int) rand());
  stream->enc = th_encode_alloc(&info);
  ogg_packet packet;
  while (th_encode_flushheader(stream->enc, &comment, &packet) > 0) {
    ogg_stream_packetin(&stream->ogg, &packet);
    stream->flushPage();
  }

  th_info_clear(&info);
  th_comment_clear(&comment);

  return true;
}

void TheoraEncoder::submitFrame(VideoFrame &frame, bool last) {
  th_ycbcr_buffer buf;

  buf[0].width  = frame.width;
  buf[0].height = frame.height;
  buf[0].stride = frame.stride;
  buf[0].data   = frame.Y;

  buf[1].width  = frame.width;
  buf[1].height = frame.height;
  buf[1].stride = frame.stride;
  buf[1].data   = frame.Cb;

  buf[2].width  = frame.width;
  buf[2].height = frame.height;
  buf[2].stride = frame.stride;
  buf[2].data   = frame.Cr;

  th_encode_ycbcr_in(stream->enc, buf);
  ogg_packet packet;
  while (th_encode_packetout(stream->enc, last, &packet) > 0) {
    ogg_stream_packetin(&stream->ogg, &packet);
    ogg_page page;
    while (ogg_stream_pageout(&stream->ogg, &page) > 0) {
      stream->writePage(page);
    }
  }

  if (last) {
    stream->flushPage();
    delete stream;
  }
}

} // namespace video
} // namespace satviz
