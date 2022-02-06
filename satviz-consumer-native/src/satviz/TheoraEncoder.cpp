#include <satviz/TheoraEncoder.hpp>

#include <fstream>
#include <theora/theoraenc.h>

#include <iostream>

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

bool TheoraEncoder::startRecording(const char *filename, int width, int height) {
  stream = new TheoraStream;

  stream->file = std::ofstream(filename, std::ofstream::binary);
  if (!stream->file.is_open()) {
    delete stream;
    stream = nullptr;
    return false;
  }

  geom.view_width  = width;
  geom.view_height = height;
  /* Theora has a divisible-by-sixteen restriction for the encoded frame size */
  /* scale the picture size up to the nearest /16 and calculate offsets */
  geom.padded_width  = (width  + 15) & ~0xF;
  geom.padded_height = (height + 15) & ~0xF;
  /*Force the offsets to be even so that chroma samples line up like we expect.*/
  geom.view_offset_x = ((geom.padded_width  - width)  >> 1) & ~1;
  geom.view_offset_y = ((geom.padded_height - height) >> 1) & ~1;

  th_info info;
  th_info_init(&info);
  info.frame_width        = geom.padded_width;
  info.frame_height       = geom.padded_height;
  info.pic_width          = geom.view_width;
  info.pic_height         = geom.view_height;
  info.pic_x              = geom.view_offset_x;
  info.pic_y              = geom.view_offset_y;
  info.colorspace         = TH_CS_UNSPECIFIED;
  info.pixel_fmt          = TH_PF_444;
  info.target_bitrate     = 0;
  info.quality            = 63; /* Between 0 and 63 inclusive */
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

  buf[0].width  = geom.padded_width;
  buf[0].height = geom.padded_height;
  buf[0].stride = frame.getStride();
  buf[0].data   = frame.Y;

  buf[1].width  = geom.padded_width;
  buf[1].height = geom.padded_height;
  buf[1].stride = frame.getStride();
  buf[1].data   = frame.Cb;

  buf[2].width  = geom.padded_width;
  buf[2].height = geom.padded_height;
  buf[2].stride = frame.getStride();
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
    std::cout << "<encoding last frame>" << std::endl;
    stream->flushPage();
    delete stream;
    stream = nullptr;
  }
}

} // namespace video
} // namespace satviz
