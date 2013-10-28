# Plots gantt charts for given scheduling logs.
#
# Has three modes:
#   - Result mode. Useful to distinguish failed and retried tasks.
#   - Workflow mode. Useful to distinguish tasks from different workflows.
#   - Storage mode. Useful to distinguish upload, download and computational part of task.
# 
# Examples of usage:
#   $ ruby plot_gantt.rb tests/test1.log results
#   $ ruby plot_gantt.rb tests/test1.log workflow
#   $ ruby plot_gantt.rb tests/test2.log storage

require 'scanf.rb'
require 'rubygems'
require 'gnuplot'
require 'set'

class TaskLog
  def initialize(workflow, id, vm, started, finished, result)
    @workflow = workflow
    @id = id
    @vm = vm
    @started = started
    @finished = finished
    @result = result
  end

  attr_reader :workflow, :id, :vm, :started, :finished, :result
end

class TransferLog
  def initialize(id, vm, started, finished, direction)
    @id = id
    @vm = vm
    @started = started
    @finished = finished
    @direction = direction
  end

  attr_reader :id, :vm, :started, :finished, :direction
end

class VMLog
  def initialize(id, started, finished)
    @id = id
    @started = started
    @finished = finished
  end

  attr_reader :id, :started, :finished
end

class Workflow
  def initialize(id, priority)
    @id = id
    @priority = priority
  end

  attr_reader :id, :priority
end

def read_log(file_content)
  lines = file_content.split(/\n/)
  current_line = 0

  vm_number = lines[current_line].to_i
  current_line += 1

  vms = Hash.new

  for i in 0...vm_number
    vm_info = lines[current_line].split

    vm = VMLog.new(vm_info[0], vm_info[1].to_f, vm_info[2].to_f)
    vms[vm.id] = vm

    current_line += 1
  end

  workflows_number = lines[current_line].to_i
  current_line += 1

  workflows = Hash.new

  for i in 0...workflows_number
    workflow_info = lines[current_line].split
    workflow = Workflow.new(workflow_info[0], workflow_info[1].to_i)
    workflows[workflow.id] = workflow
    current_line += 1
  end

  tasks_number = lines[current_line].to_i
  current_line += 1

  tasks = []

  for i in 0...tasks_number
    task_info = lines[current_line].split
    task = TaskLog.new(task_info[0], task_info[1], task_info[2], task_info[3].to_f, task_info[4].to_f, task_info[5])
    tasks.push(task)
    current_line += 1
  end

  transfers_number = lines[current_line].to_i
  current_line += 1

  transfers = []

  for i in 0...transfers_number
    transfer_info = lines[current_line].split
    transfer = TransferLog.new(transfer_info[0], transfer_info[1], transfer_info[2].to_f, transfer_info[3].to_f, transfer_info[4])
    transfers.push(transfer)
    current_line += 1
  end

  return {
    :vms => vms,
    :workflows => workflows,
    :tasks => tasks,
    :transfers => transfers
  }
end

def read_log_from_file(filename)
  file_content = `cat #{filename}`
  return read_log(file_content)
end

class GanttPlotter
  def initialize()
    @data = []
    @colors = {
      :red => 'red',
      :blue => 'grey90',
      :green => 'green',
      :orange => 'orange',
      :brown => 'brown', 
      :dark_grey => 'grey10'
    }
    @types = {
      :dotted => 0,
      :straight => 1
    }
    @styles = {}
    @style_next_id = 1
  end

  def create_gantt_series (startsList, finishesList, rows, line_style, title)
    return Gnuplot::DataSet.new( [startsList, rows, startsList, finishesList] ) do |ds|
      ds.using = "($1):2:3:4:($2-0.4):($2+0.4):yticlabels(2) ls #{line_style}"
      ds.with = "boxxyerrorbars fs solid 0.55"
      ds.title = title
    end
  end

  def get_line_style(color, type)
    return @styles[[color, type]]
  end

  def add_series(series, title, color, type=:straight)
    vm_row = series[:vms].collect { |vm| vm.sub("VM", "").to_i }
    started_row = series[:started]
    finished_row = series[:finished]

    if vm_row.empty?
      return
    end

    add_style_line_if_not_exist color, type
    line_style = get_line_style color, type

    makespans = create_gantt_series(started_row, finished_row, vm_row, line_style, title)
    @data.push(makespans)
  end

  def add_style_line_if_not_exist(color, type)
    key = [color, type]
    if not @styles.has_key? key
      @styles[key] = @style_next_id
      @style_next_id += 1
    end
  end

  def plot(filename)
    Gnuplot.open do |gp|
      Gnuplot::Plot.new( gp ) do |plot|
        @styles.each do |key, style_id|
          color, type = key
          gp_type = @types[type]
          gp_color = @colors[color]
          plot.set "style line #{style_id} lc rgb '#{gp_color}' lt #{gp_type} lw 1"
        end
        plot.set "style fill border lc rgb 'black'"
        

        #plot.title  "Schedule " + File.basename(filename)
        plot.xlabel "Time"
        plot.ylabel "VM"
        #plot.xtics 3600
        plot.ytics 1
        #plot.yrange "[-0.8:#{ymax}]"
        #plot.terminal 'pdfcairo size 5,1.5 font "arial,8" linewidth 1'
        #plot.terminal 'pdf size 11,8.5 font "arial,6" linewidth 1'
        plot.set "key right outside"
        # plot.set "key off"
        #plot.noytics
        #plot.noxtics
        #plot.set "grid"
        plot.terminal "png size 1024,768"
        plot.output filename + ".png"

        plot.data = @data
      end
    end
  end
end

def get_task_series(tasks)
  return {
    :vms => tasks.collect { |task| task.vm },
    :started => tasks.collect { |task| task.started },
    :finished => tasks.collect { |task| task.finished }
  }
end

def plot_result_schedule (logs, filename)
  plotter = GanttPlotter.new

  vms = logs[:vms].values
  provisioning_series = {
    :vms => vms.collect { |vm| vm.id },
    :started => vms.collect { |vm| vm.started },
    :finished => vms.collect { |vm| vm.finished }
  }
  plotter.add_series provisioning_series, "VM idle", :blue
 
  tasks = logs[:tasks]

  finished_tasks = tasks.select { |task| task.result.include? "OK" and not task.result.include? "RETRY" }
  plotter.add_series get_task_series(finished_tasks), "Done", :green

  failed_tasks = tasks.select { |task| task.result.include? "FAILED" and not task.result.include? "RETRY"  }
  plotter.add_series get_task_series(failed_tasks), "Failed", :red

  retried_finished_tasks = tasks.select { |task| task.result.include? "OK" and task.result.include? "RETRY" }
  plotter.add_series get_task_series(retried_finished_tasks), "Retry", :green, :dotted

  retried_failed_tasks = tasks.select { |task| task.result.include? "FAILED" and task.result.include? "RETRY" }
  plotter.add_series get_task_series(retried_failed_tasks), "Retry failed", :red, :dotted

  plotter.plot(filename)
end

def plot_workflow_schedule(logs, filename)
  plotter = GanttPlotter.new

  vms = logs[:vms].values
  provisioning_series = {
    :vms => vms.collect { |vm| vm.id },
    :started => vms.collect { |vm| vm.started },
    :finished => vms.collect { |vm| vm.finished }
  }
  plotter.add_series provisioning_series, "VM idle", :blue
 
  tasks = logs[:tasks]
  tasks_by_workflow = tasks.group_by { |task| task.workflow }

  workflows = logs[:workflows].values

  colors = [:red, :green, :orange, :dark_grey, :brown]

  workflows.reverse.each_with_index do |workflow, i|
    color = colors[i % colors.length]
    workflow_tasks = tasks_by_workflow[workflow.id]
    plotter.add_series get_task_series(workflow_tasks), "#{workflow.id} (#{workflow.priority})" , color
  end

  plotter.plot(filename)
end

def plot_storage_schedule(logs, filename)
  plotter = GanttPlotter.new

  vms = logs[:vms].values
  provisioning_series = {
    :vms => vms.collect { |vm| vm.id },
    :started => vms.collect { |vm| vm.started },
    :finished => vms.collect { |vm| vm.finished }
  }
  plotter.add_series provisioning_series, "VM idle", :blue
 
  tasks = logs[:tasks]
  plotter.add_series get_task_series(tasks), "Computation", :dark_grey

  transfers = logs[:transfers]
  input_transfers = transfers.select { |transfer| transfer.direction == "UPLOAD" }
  plotter.add_series get_task_series(input_transfers), "Upload", :orange

  output_transfers = transfers.select { |transfer| transfer.direction == "DOWNLOAD" }
  plotter.add_series get_task_series(output_transfers), "Download", :green

  plotter.plot(filename)
end

log_filename = ARGV[0]
type = ARGV[1]
output_filename = "test"

logs = read_log_from_file(log_filename)

case type
when "results"
  plot_result_schedule(logs, output_filename)
when "workflows"
  plot_workflow_schedule(logs, output_filename)
when "storage"
  plot_storage_schedule(logs, output_filename)
end
