package com.kamennova.lala;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TimedLala {
    public static void main(String[] args) {
        // goose
        List<LaLaLearn.RNote> melody1 = List.of(
                new LaLaLearn.RNote(5, 4),
                new LaLaLearn.RNote(4, 4),
                new LaLaLearn.RNote(2, 4),
                new LaLaLearn.RNote(0, 4),

                new LaLaLearn.RNote(7, 8),
                new LaLaLearn.RNote(7, 8));


        List<LaLaLearn.RNote> melody2 = List.of(
                new LaLaLearn.RNote(5, 4),
                new LaLaLearn.RNote(4, 4),
                new LaLaLearn.RNote(2, 4),
                new LaLaLearn.RNote(0, 4),

                new LaLaLearn.RNote(7, 8),
                new LaLaLearn.RNote(7, 8));

    }
    private class NoteSeq{
        public List<LaLaLearn.RNote> notes;

        NoteSeq(List<LaLaLearn.RNote> notes) {
            this.notes = notes;
        }

        public int getDuration(){
            return this.notes.stream().map(n -> (int) n.duration).reduce(Integer::sum).get();
        }

        @Override
        public int hashCode() {
            return this.getDuration();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final NoteSeq other = (NoteSeq) obj;

            return true; // todo
        }
    }

    private List<List<NoteSeq>> getTimeSequences(List<LaLaLearn.RNote> notes, int totalDuration, int batchDuration) {
        int startsNum = totalDuration / batchDuration;

        List<List<NoteSeq>> sequences = new ArrayList<>();

        for (int i = 0; i < startsNum; i++) {
            List<NoteSeq> curr = timeBatches(notes.subList(i, notes.size()), batchDuration);
            System.out.println(curr.get(0));
            sequences.add(curr);
        }

        return sequences;
    }


    private List<NoteSeq> timeBatches(List<LaLaLearn.RNote> source, int duration) {
        List<NoteSeq> res = new ArrayList<>();
        List<LaLaLearn.RNote> currSeq = new ArrayList<>();
        int currDuration = 0;

        for (LaLaLearn.RNote currNote : source) {
            int durationLeft = duration - currDuration;
            if (currNote.duration >= durationLeft) {
                currSeq.add(new LaLaLearn.RNote(currNote.interval, durationLeft));
                res.add(new NoteSeq(currSeq));
                currSeq = new ArrayList<>();
                currDuration = 0;
            } else {
                currSeq.add(currNote);
                currDuration += currNote.duration;
            }
        }

        return res;
    }

    private HashMap<NoteSeq, Integer> timedStore3 = new HashMap<>();
}
