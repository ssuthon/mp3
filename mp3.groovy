@Grab('com.mpatric:mp3agic:0.8.3')
import com.mpatric.mp3agic.*
import groovy.io.*

def sim = false
def dir = new File('D:/mp3/EFM150926')
def output = new File('D:/mp3/EFM150926x')
if(!output.exists()){
    output.mkdirs()
}
def log = new File('D:/mp3.log')

def banned = """[AeLOAD]
Music4load.net
Upload4u
Pleng4load.com
Pleng4load
(Mp3For.Me)
@*BY Vampire-NeW*@
By LOAD2U.COM
""".split('\n')

def p = { text ->
    if(!text) return null
    
    banned.each {
        text = text - it
    }
    def fc = text.charAt(0)
    def ok = (fc >= 'a' && fc <= 'z') ||
               (fc >= 'A' && fc <= 'Z') ||
               (fc >= '0' && fc <= '9') ||
               (fc >= 0xE01  && fc <= 0xE5B)
               
    text = ok ? text : new String(text.getBytes("ISO8859-1"), "TIS-620")
    text.trim()
}
log.withWriter('UTF-8') { writer ->
    dir.eachFileRecurse (FileType.FILES) { file ->
      def mf = new Mp3File(file)
      def otag = mf.hasId3v2Tag() ? mf.id3v2Tag : mf.id3v1Tag
      def ntag = new ID3v24Tag()
      
      ntag.title = otag?.title ? p(otag.title) : file.name.substring(2).split('-')[0].trim()
      ntag.artist = otag?.artist || otag?.albumArtist ? ( p(otag.artist) ?: p(otag.albumArtist) ) :  (file.name.substring(2) - '.mp3').split('-')[1].trim()
      ntag.track = file.name.substring(0,2)
      ntag.albumArtist = dir.name
      ntag.album = dir.name
      
      writer.println ntag.track + ":" + ntag.title + " : " + ntag.artist
      
      if(!sim){
          if (mf.hasId3v1Tag()) {
              mf.removeId3v1Tag();
          }
          if (mf.hasId3v2Tag()) {
              mf.removeId3v2Tag();
          }
          mf.id3v2Tag = ntag
          mf.save(new File(output, file.name).absolutePath)
      }
    }
    writer.close()
}